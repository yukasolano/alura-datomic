(ns curso6.db.categoria
  (:require [datomic.api :as d]
            [schema.core :as s]
            [curso6.model :as model]
            [curso6.db.entidade :as db.entidade]))

(s/defn get-all :- [model/Categoria] [db]
  (db.entidade/datomic-para-entidade
   (d/q '[:find [(pull ?entidade [*]) ...]
          :where [?entidade :categoria/id]] db)))

(s/defn add
  [conn categorias :- [model/Categoria]]
  (d/transact conn categorias))

