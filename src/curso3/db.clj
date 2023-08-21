(ns curso3.db
  (:require [datomic.api :as d]
            [schema.core :as s]
            [curso3.model :as model]
            [clojure.walk :as walk])
  (:import (java.util UUID)))

(def db-url "datomic:dev://localhost:4334/hello")

(defn abre-conexao []
  (d/create-database db-url)
  (d/connect db-url))

(defn apaga-banco []
  (d/delete-database db-url))

(def schema [{:db/ident       :produto/nome
              :db/valueType   :db.type/string
              :db/cardinality :db.cardinality/one
              :db/doc         "O nome de um produto"}
             {:db/ident       :produto/slug
              :db/valueType   :db.type/string
              :db/cardinality :db.cardinality/one
              :db/doc         "O caminho para acessar esse produto via http"}
             {:db/ident       :produto/preco
              :db/valueType   :db.type/bigdec
              :db/cardinality :db.cardinality/one
              :db/doc         "O preço de um produto com precisão monetária"}
             {:db/ident       :produto/palavra-chave
              :db/valueType   :db.type/string
              :db/cardinality :db.cardinality/many}
             {:db/ident       :produto/id
              :db/valueType   :db.type/uuid
              :db/cardinality :db.cardinality/one
              :db/unique      :db.unique/identity}
             {:db/ident       :produto/categoria
              :db/valueType   :db.type/ref
              :db/cardinality :db.cardinality/one}
             {:db/ident       :produto/estoque
              :db/valueType   :db.type/long
              :db/cardinality :db.cardinality/one}
             {:db/ident       :produto/digital
              :db/valueType   :db.type/boolean
              :db/cardinality :db.cardinality/one}

             ;categorias
             {:db/ident       :categoria/nome
              :db/valueType   :db.type/string
              :db/cardinality :db.cardinality/one}
             {:db/ident       :categoria/id
              :db/valueType   :db.type/uuid
              :db/cardinality :db.cardinality/one
              :db/unique      :db.unique/identity}

             ;transacoes
             {:db/ident       :tx-data/ip
              :db/valueType   :db.type/string
              :db/cardinality :db.cardinality/one}])

(defn cria-schema [conn]
  (d/transact conn schema))

(s/defn adiciona-categorias
  [conn categorias :- [model/Categoria]]
  (d/transact conn categorias))

(s/defn adiciona-produtos
  ([conn produtos :- [model/Produto]]
   (d/transact conn produtos))
  ([conn produtos :- [model/Produto] ip]
   (let [db-add-ip [:db/add "datomic.tx" :tx-data/ip ip]]
     (d/transact conn (conj produtos db-add-ip)))))

(defn dissoc-db-id [entidade]
  (if (map? entidade)
    (dissoc entidade :db/id)
    entidade))

(defn datomic-para-entidade [entidades]
  (walk/prewalk dissoc-db-id entidades))

(s/defn todas-as-categorias :- [model/Categoria] [db]
  (datomic-para-entidade
   (d/q '[:find [(pull ?entidade [*]) ...]
          :where [?entidade :categoria/id]] db)))

(s/defn todos-os-produtos :- [model/Produto] [db]
  (datomic-para-entidade
   (d/q '[:find [(pull ?entidade [* {:produto/categoria [*]}]) ...]
          :where [?entidade :produto/nome]] db)))

(defn db-adds-de-atribuicao-de-categorias [produtos categoria]
  (reduce (fn [db-adds produto]
            (conj db-adds [:db/add [:produto/id (:produto/id produto)]
                           :produto/categoria
                           [:categoria/id (:categoria/id categoria)]]))
          [] produtos))

(defn atribui-categorias [conn produtos categoria]
  (let [a-transacaionar (db-adds-de-atribuicao-de-categorias produtos categoria)]
    (d/transact conn a-transacaionar)))

(s/defn um-produto :- (s/maybe model/Produto) [db uuid :- UUID]
  (let [produto (datomic-para-entidade (d/pull db '[* {:produto/categoria [*]}] [:produto/id uuid]))]
    (if (:produto/id produto)
      produto
      nil)))

(s/defn um-produto! :- model/Produto [db uuid :- UUID]
  (let [produto (um-produto db uuid)]
    (if (nil? produto)
      (throw (ex-info "Não encontrei uma entidade" {:type :erros/not-found, :id uuid}))
      produto)))

;regra busca estoque ou se digital true es`toque é 100
(def regras
  '[[(estoque ?produto ?estoque)           ; * rule 1
     [?produto :produto/estoque ?estoque]] ; it needs to have estoque
    [(estoque ?produto ?estoque)           ; * rule 2
     [?produto :produto/digital true]      ; or it needs to be digital
     [(ground 100) ?estoque]]              ; and returns a default value of 100
    [(pode-vender? ?produto)               ; * rule 3
     (estoque ?produto ?estoque)           ; get estoque
     [(> ?estoque 0)]]])                   ; check if estoque > 0

(s/defn todos-os-produtos-com-estoque :- [model/Produto] [db]
  (datomic-para-entidade
   (d/q '[:find [(pull ?produto [* {:produto/categoria [*]}]) ...]
          :in $ %
          :where (pode-vender? ?produto)] db regras)))


(s/defn um-produto-com-estoque :- (s/maybe model/Produto) [db produto-id :- UUID]
  (let [query     '[:find (pull ?produto [* {:produto/categoria [*]}]) .
                    :in $ % ?produto-id
                    :where [?produto :produto/id ?produto-id]
                    (pode-vender? ?produto)]
        resultado (d/q query db regras produto-id)
        produto   (datomic-para-entidade resultado)]
    (if (:produto/id produto)
      produto
      nil)))
