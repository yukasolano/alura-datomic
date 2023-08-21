(ns curso4.db
  (:require [datomic.api :as d]
            [schema.core :as s]
            [curso4.model :as model]
            [clojure.walk :as walk]
            [clojure.set :as cset])
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
             {:db/ident       :produto/variacao
              :db/valueType   :db.type/ref
              :db/isComponent true
              :db/cardinality :db.cardinality/many}
             {:db/ident       :produto/visualizacoes
              :db/valueType   :db.type/long
              :db/noHistory   true
              :db/cardinality :db.cardinality/one}

             ;variaca
             {:db/ident       :variacao/id
              :db/valueType   :db.type/uuid
              :db/cardinality :db.cardinality/one
              :db/unique      :db.unique/identity}
             {:db/ident       :variacao/nome
              :db/valueType   :db.type/string
              :db/cardinality :db.cardinality/one}
             {:db/ident       :variacao/preco
              :db/valueType   :db.type/bigdec
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
              :db/cardinality :db.cardinality/one}
             ])

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
  '[[(estoque ?produto ?estoque)
     [?produto :produto/estoque ?estoque]]
    [(estoque ?produto ?estoque)
     [?produto :produto/digital true]
     [(ground 100) ?estoque]]
    [(pode-vender? ?produto)
     (estoque ?produto ?estoque)
     [(> ?estoque 0)]]
    [(produto-na-categoria ?produto ?nome-da-categoria)
     [?categoria :categoria/nome ?nome-da-categoria]
     [?produto :produto/categoria ?categoria]]])

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

(s/defn todos-os-produtos-nas-categorias :- [model/Produto]
  [db categorias :- [s/Str]]
  (datomic-para-entidade
   (d/q '[:find [(pull ?produto [* {:produto/categoria [*]}]) ...]
          :in $ % [?nome-da-categoria ...]
          :where
          (produto-na-categoria ?produto ?nome-da-categoria)]
        db regras categorias))
  )

(s/defn todos-os-produtos-nas-categorias-e-digital :- [model/Produto]
  [db categorias :- [s/Str] digital? :- s/Bool]
  (datomic-para-entidade
   (d/q '[:find [(pull ?produto [* {:produto/categoria [*]}]) ...]
          :in $ % [?nome-da-categoria ...] ?digital
          :where
          (produto-na-categoria ?produto ?nome-da-categoria)
          [?produto :produto/digital ?digital]]
        db regras categorias digital?))
  )

(s/defn atualiza-preco!
  [conn produto-id :- UUID preco-antigo :- BigDecimal preco-novo :- BigDecimal]
  (d/transact conn [[:db/cas [:produto/id produto-id] :produto/preco preco-antigo preco-novo]]))

(s/defn atualiza-produto!
  [conn antigo :- model/Produto a-atualizar :- model/Produto]
  (let [produto-id (:produto/id antigo)
        atributos  (cset/intersection (set (keys antigo)) (set (keys a-atualizar)))
        atributos  (disj atributos :produto/id)
        txs        (map (fn [atributo] [:db/cas [:produto/id produto-id] atributo (get antigo atributo) (get a-atualizar atributo)]) atributos)]
    (d/transact conn txs)))

;Usa um db-id temporario para facilitar a referência entre variacao e produto
(s/defn adiciona-variacao!
  [conn, produto-id :- UUID, variacao :- s/Str, preco :- BigDecimal]
  (d/transact conn [{:db/id          "variacao-temporaria"
                     :variacao/nome  variacao
                     :variacao/preco preco
                     :variacao/id    (model/uuid)}
                    {:produto/id       produto-id
                     :produto/variacao "variacao-temporaria"}]))

(defn total-de-produtos [db]
  (d/q '[:find [(count ?produto)]
         :where [?produto :produto/nome]]
       db))

(s/defn remove-produto! [conn produto-id :- UUID]
  (d/transact conn [[:db/retractEntity [:produto/id produto-id]]]))

(s/defn visualizacoes [db produto-id :- UUID]
  (or (d/q '[:find ?visualizacoes .
             :in $ ?produto-id
             :where [?p :produto/id ?produto-id]
             [?p :produto/visualizacoes ?visualizacoes]]
           db produto-id)
      0))

(s/defn adiciona-visualizacao [conn produto-id :- UUID]
  (let [visualizacoes (visualizacoes (d/db conn) produto-id)
        novo-valor    (inc visualizacoes)]
    (d/transact conn [{:produto/id            produto-id
                       :produto/visualizacoes novo-valor}]))
  )

(s/defn adiciona-visualizacao-com-atomicidade [conn produto-id :- UUID]
  (d/transact conn [[:incrementa-visualziacao produto-id]]))
