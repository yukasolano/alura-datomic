(ns curso6.db.produto
  (:require [datomic.api :as d]
            [schema.core :as s]
            [curso6.model :as model]
            [clojure.set :as cset]
            [curso6.db.entidade :as db.entidade])
  (:import (java.util UUID)))

(s/defn get-all :- [model/Produto] [db]
  (db.entidade/datomic-para-entidade
   (d/q '[:find [(pull ?entidade [* {:produto/categoria [*]}]) ...]
          :where [?entidade :produto/id]] db)))

(s/defn get-one :- (s/maybe model/Produto) [db uuid :- UUID]
  (let [produto (db.entidade/datomic-para-entidade (d/pull db '[* {:produto/categoria [*]}] [:produto/id uuid]))]
    (if (:produto/id produto)
      produto
      nil)))

(defn total [db]
  (d/q '[:find [(count ?produto)]
         :where [?produto :produto/nome]]
       db))

(s/defn add
  ([conn produtos :- [model/Produto]]
   (d/transact conn produtos))
  ([conn produtos :- [model/Produto] ip]
   (let [db-add-ip [:db/add "datomic.tx" :tx-data/ip ip]]
     (d/transact conn (conj produtos db-add-ip)))))

(s/defn remove-entity [conn produto-id :- UUID]
  (d/transact conn [[:db/retractEntity [:produto/id produto-id]]]))


;regra busca estoque ou se digital true es`toque é 100
(def rules
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

;;; STOCK
(s/defn get-all-with-stock :- [model/Produto] [db]
  (db.entidade/datomic-para-entidade
   (d/q '[:find [(pull ?produto [* {:produto/categoria [*]}]) ...]
          :in $ %
          :where (pode-vender? ?produto)] db rules)))


(s/defn get-with-stock :- (s/maybe model/Produto) [db produto-id :- UUID]
  (let [query     '[:find (pull ?produto [* {:produto/categoria [*]}]) .
                    :in $ % ?produto-id
                    :where [?produto :produto/id ?produto-id]
                    (pode-vender? ?produto)]
        resultado (d/q query db rules produto-id)
        produto   (db.entidade/datomic-para-entidade resultado)]
    (if (:produto/id produto)
      produto
      nil)))

;;; CATEGORY
(defn db-adds-of-category-link [produtos categoria]
  (reduce (fn [db-adds produto]
            (conj db-adds [:db/add [:produto/id (:produto/id produto)]
                           :produto/categoria
                           [:categoria/id (:categoria/id categoria)]]))
          [] produtos))

(defn add-category [conn produtos categoria]
  (let [a-transacionar (db-adds-of-category-link produtos categoria)]
    (d/transact conn a-transacionar)))

(s/defn get-all-by-categories :- [model/Produto]
  [db categorias :- [s/Str]]
  (db.entidade/datomic-para-entidade
   (d/q '[:find [(pull ?produto [* {:produto/categoria [*]}]) ...]
          :in $ % [?nome-da-categoria ...]
          :where
          (produto-na-categoria ?produto ?nome-da-categoria)]
        db rules categorias))
  )

(s/defn get-all-digital-by-categories :- [model/Produto]
  [db categorias :- [s/Str] digital? :- s/Bool]
  (db.entidade/datomic-para-entidade
   (d/q '[:find [(pull ?produto [* {:produto/categoria [*]}]) ...]
          :in $ % [?nome-da-categoria ...] ?digital
          :where
          (produto-na-categoria ?produto ?nome-da-categoria)
          [?produto :produto/digital ?digital]]
        db rules categorias digital?)))

(s/defn update-price
  [conn produto-id :- UUID preco-antigo :- BigDecimal preco-novo :- BigDecimal]
  (d/transact conn [[:db/cas [:produto/id produto-id] :produto/preco preco-antigo preco-novo]]))

(s/defn update-entity
  [conn antigo :- model/Produto a-atualizar :- model/Produto]
  (let [produto-id (:produto/id antigo)
        atributos  (cset/intersection (set (keys antigo)) (set (keys a-atualizar)))
        atributos  (disj atributos :produto/id)
        txs        (map (fn [atributo] [:db/cas [:produto/id produto-id] atributo (get-one antigo atributo) (get-one a-atualizar atributo)]) atributos)]
    (d/transact conn txs)))

;;; VARIACAO
;Usa um db-id temporario para facilitar a referência entre variacao e produto
(s/defn adiciona-variacao!
  [conn, produto-id :- UUID, variacao :- s/Str, preco :- BigDecimal]
  (d/transact conn [{:db/id          "variacao-temporaria"
                     :variacao/nome  variacao
                     :variacao/preco preco
                     :variacao/id    (model/uuid)}
                    {:produto/id       produto-id
                     :produto/variacao "variacao-temporaria"}]))


;;; VIEWS
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
  (d/transact conn [[:incrementa-visualizacao produto-id]]))

;Transactional function
(def incrementa-visualizacao
  #db/fn {:lang   :clojure
          :params [db produto-id]
          :code   (let [visualizacoes (or (d/q '[:find ?visualizacoes .
                                                 :in $ ?produto-id
                                                 :where [?p :produto/id ?produto-id]
                                                 [?p :produto/visualizacoes ?visualizacoes]]
                                               db produto-id)
                                          0)
                        novo-valor    (inc visualizacoes)]
                    [{:produto/id            produto-id
                      :produto/visualizacoes novo-valor}])})
;Instala a funcao
(defn add-transaction-function [conn]
  @(d/transact conn [{:db/doc   "Incrementa visualizacoes"
                      :db/ident :incrementa-visualizacao
                      :db/fn    incrementa-visualizacao}]))


(s/defn historico-de-precos [db produto-id :- UUID]
  (->> (d/q '[:find ?instante ?preco
              :in $ ?produto-id
              :where [?p :produto/id ?produto-id]
              [?p :produto/preco ?preco ?tx true]
              [?tx :db/txInstant ?instante]]
            (d/history db) produto-id)
       (sort-by first)))

(defn busca-mais-caro [db]
  (d/q '[:find (max ?preco) .
         :where [_ :produto/preco ?preco]]
       db))

(defn busca-mais-caro-que [db preco-minimo]
  (d/q '[:find ?preco
         :in $ ?preco-minimo
         :where [_ :produto/preco ?preco]
         [(>= ?preco ?preco-minimo)]]
       db preco-minimo))

(defn busca-por-preco [db preco]
  (db.entidade/datomic-para-entidade
   (d/q '[:find (pull ?produto [*])
          :in $ ?preco
          :where [?produto :produto/preco ?preco]]
        db preco)))

(defn busca-por-preco-e-nome-otimizado [db preco nome-parcial]
  (db.entidade/datomic-para-entidade
   (d/q '[:find (pull ?produto [*])
          :in $ ?preco ?nome-parcial
          :where [?produto :produto/preco ?preco]
          [?produto :produto/nome ?nome]
          [(clojure.string/includes? ?nome ?nome-parcial)]]
        db preco nome-parcial)))

(defn busca-por-preco-e-nome [db preco nome-parcial]
  (db.entidade/datomic-para-entidade
   (d/q '[:find (pull ?produto [*])
          :in $ ?preco ?nome-parcial
          :where [?produto :produto/nome ?nome]
          [(clojure.string/includes? ?nome ?nome-parcial)]
          [?produto :produto/preco ?preco]]
        db preco nome-parcial)))
