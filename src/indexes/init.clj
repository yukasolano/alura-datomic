(ns indexes.init
  (:use clojure.pprint)
  (:import (java.util UUID))
  (:require [datomic.api :as d]
            [indexes.db.config :as db.config]
            [schema.core :as s]
            [schema-generators.generators :as g]
            [indexes.generators :as generators]))


(s/def Person
  {:person/id UUID
   :person/name s/Str
   :person/cpf s/Str})

(s/def Home
  {:home/address s/Str
   :home/person Person})

(s/defn get-all-people
  [db]
  (d/q '[:find [(pull ?entidade [*]) ...]
         :where [?entidade :person/cpf]] db))

(s/defn get-all-homes
  [db]
  (d/q '[:find [(pull ?entidade [*]) ...]
         :where [?entidade :home/address]] db))

(s/defn add-all
  [conn people]
  (d/transact conn people))


;;;;;;;;;;;;;;;;;;;;;;

(db.config/apaga-banco)
(def conn (db.config/abre-conexao))

;Create schemas
(db.config/cria-schema conn)

(s/set-fn-validation! true)

;(println "### Generates a sample of 10 people")
;(def sample (g/sample 10 Person generators/leaf-generators))
;(pprint sample)
;
;(println "### Add sample to database")
;(pprint @(add-all conn sample))
;
;(println "### Get all")
;(pprint (get-all-people (d/db conn)))
;
;(println "### Generates a sample of 10 homes")
;(def sample (g/sample 10 Home generators/leaf-generators))
;(pprint sample)
;
;(println "### Add sample to database")
;(pprint @(add-all conn sample))
;
;(println "### Get all")
;(pprint (get-all-homes (d/db conn)))

;(defn generate-10000-people [conn]
;  (dotimes [atual 50]
;    (def sample (g/sample 200 Person generators/leaf-generators))
;    (println atual (count @(add conn sample)))))
;
;(println "### Time to generate 10000 people and to the database")
;(time (generate-10000-people conn))

(defn generate-10000-homes [conn]
  (dotimes [atual 50]
    (def sample (g/sample 200 Home generators/leaf-generators))
    (println atual (count @(add-all conn sample)))))

(println "### Time to generate 10000 homes and to the database")
(time (generate-10000-homes conn))
