(ns curso6.db.venda
  (:require [datomic.api :as d]
            [schema.core :as s]
            [curso6.model :as model]
            [curso6.db.entidade :as db.entidade])
  (:import (java.util UUID)))

(s/defn get-all :- [model/Venda] [db]
  (db.entidade/datomic-para-entidade
   (d/q '[:find [(pull ?entidade [* {:venda/produto [* {:produto/categoria [*]}]}]) ...]
          :where [?entidade :venda/id]] db)))

(s/defn add
  [conn
   produto-id :- UUID
   quantidade :- s/Int]
  (let [id (model/uuid)]
    (d/transact conn
                [{:db/id            "venda"
                  :venda/id         id
                  :venda/produto    [:produto/id produto-id] ;lookup ref
                  :venda/quantidade quantidade
                  :venda/status     "nova"}])
    id))

(s/defn custo
  [db
   venda-id :- UUID]
  (d/q '[:find ?custo-venda .
         :in $ ?venda-id
         :where [?v :venda/id ?venda-id]
         [?v :venda/quantidade ?quantidade]
         [?v :venda/produto ?p]
         [?p :produto/preco ?preco]
         [(* ?quantidade ?preco) ?custo-venda]]
       db venda-id))

(s/defn instante-da-venda
  [db
   venda-id :- UUID]
  (d/q '[:find ?instante .
         :in $ ?venda-id
         :where [?v :venda/id ?venda-id ?tx true]
         [?tx :db/txInstant ?instante]]
       db venda-id))

(s/defn custo-no-instante-da-venda
  [db
   venda-id :- UUID]
  (let [instante (instante-da-venda db venda-id)]
    (custo (d/as-of db instante) venda-id)))

(s/defn cancel [conn venda-id :- UUID]
  (d/transact conn [[:db/retractEntity [:venda/id venda-id]]]))

;somente vendas ativas (nao canceladas)
(s/defn get-all-ids [db]
  (d/q '[:find ?id
         :where [?v :venda/id ?id]]
       db))

;somente vendas cancelas
(s/defn get-all-ids-canceled [db]
  (d/q '[:find ?id
         :where [?v :venda/id ?id _ false]]
       (d/history db)))

;todas as vendas (ativas e cancelas)
(s/defn get-all-ids-inclusive-canceled [db]
  (d/q '[:find ?id
         :where [?v :venda/id ?id _ true]]
       (d/history db)))

(s/defn update-status
  [conn
   venda-id :- UUID
   novo-status :- s/Str]
  (d/transact conn [{:venda/id     venda-id
                     :venda/status novo-status}]))

(s/defn historico
  [db
   venda-id :- UUID]
  (->> (d/q '[:find ?instante ?status
              :in $ ?venda-id
              :where [?v :venda/id ?venda-id]
              [?v :venda/status ?status ?tx true]
              [?tx :db/txInstant ?instante]]
            (d/history db) venda-id)
       (sort-by first)))

;nova versao de cancelada agora usando status
;somente vendas ativas (nao canceladas)
(s/defn get-all-ids-status-not-canceled [db]
  (d/q '[:find ?id
         :where [?v :venda/id ?id]
         [?v :venda/status ?status]
         [(not= ?status "cancelada")]]
       db))

;somente vendas cancelas
(s/defn get-all-ids-status-canceled [db]
  (d/q '[:find ?id
         :where [?v :venda/id ?id]
         [?v :venda/status "cancelada"]]
       db))

;todas as vendas (ativas e cancelas)
(s/defn get-all-ids-status-inclusive-canceled [db]
  (d/q '[:find ?id
         :where [?v :venda/id ?id]]
       db))

(defn historico-geral [db instante-desde]
  (->> (d/q '[:find ?instante ?id ?status
              :in $  $filtrado
              :where [$ ?v :venda/id ?id]
              [$filtrado ?v :venda/status ?status ?tx true]
              [$filtrado ?tx :db/txInstant ?instante]]
             db (d/since db instante-desde))
       (sort-by first)))
