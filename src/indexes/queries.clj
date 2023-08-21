(ns indexes.queries
  (:use clojure.pprint)
  (:require [datomic.api :as d]
            [schema-generators.generators :as g]
            [indexes.generators :as generators]
            [indexes.init :as init]))


(def home (g/generate init/Home generators/leaf-generators))
(init/add-all init/conn [home])


(defn busca-mais-caro-que [db preco-minimo]
  (d/q '[:find ?preco
         :in $ ?preco-minimo
         :where [_ :produto/preco ?preco]
         [(>= ?preco ?preco-minimo)]]
       db preco-minimo))

(defn busca-por-preco [db preco]
  (d/q '[:find (pull ?produto [*])
            :in $ ?preco
            :where [?produto :produto/preco ?preco]]
          db preco))


;(println "Levou o tempo a seguir para achar o produto mais caro:")
;(time (dotimes [_ 100] (db.produto/busca-mais-caro (d/db conn))))

; Dado person id devolve entidade de person {id, name, cpf}
(defn search-by-person-id [db person-id]
  (d/q '[:find (pull ?person [*]) .
         :in $ ?id
         :where [?person :person/id ?id]]
       db person-id))

(defn search-by-person-name [db person-name]
  (d/q '[:find (pull ?person [*]) .
         :in $ ?name
         :where [?person :person/name ?name]]
       db person-name))

(defn search-by-person-cpf [db person-cpf]
  (d/q '[:find (pull ?person [*]) .
         :in $ ?cpf
         :where [?person :person/cpf ?cpf]]
       db person-cpf))

(defn lookup-by-person-id [db person-id]
  (d/pull db '[*] [:person/id person-id]))

;(time (dotimes [_ 1000] (lookup-by-person-id (d/db init/conn) (-> home :home/person :person/id)))) ; 1o
;(time (dotimes [_ 1000] (search-by-person-id (d/db init/conn) (-> home :home/person :person/id)))) ;2o -    :db/unique  :db.unique/identity
;(time (dotimes [_ 1000] (search-by-person-cpf (d/db init/conn) (-> home :home/person :person/cpf)))) ;2o -  :db/index   true
;(time (dotimes [_ 1000] (search-by-person-name (d/db init/conn) (-> home :home/person :person/name)))) ;3o




;Dado person id devolve entidade person e home
(defn search-home-by-person-id [db person-id]
  (d/q '[:find (pull ?person [* {:home/_person [*]}]) .
         :in $ ?id
         :where [?person :person/id ?id]]
       db person-id))

(defn search-home-by-person-id-2 [db person-id]
  (d/q '[:find (pull ?home [* {:home/person [*]}]) .
         :in $ ?id
         :where [?person :person/id ?id]
         [?home :home/person ?person]]
       db person-id))

(defn lookup-by-person-id [db person-id]
  (d/pull db '[* {:home/_person [*]}] [:person/id person-id]))

(defn lookup-by-person-id-2 [db person-id]
  (let [person (d/pull db '[*] [:person/id person-id])
        home  (d/pull db '[{:home/_person [*]}] [:person/id person-id])]
    (merge person home)))

(defn search-home-by-person-id-aux [db person-id]
  (d/q '[:find (pull ?home [*]) .
         :in $ ?id
         :where [?person :person/id ?id]
         [?home :home/person ?person]]
       db person-id))

(defn lookup-by-person-id-3 [db person-id]
  (let [person (d/pull db '[*] [:person/id person-id])
        home  (search-home-by-person-id-aux db person-id)]
    (merge person home)))

(pprint (lookup-by-person-id (d/db init/conn) (-> home :home/person :person/id)))
(pprint (lookup-by-person-id-2 (d/db init/conn) (-> home :home/person :person/id)))
(pprint (lookup-by-person-id-3 (d/db init/conn) (-> home :home/person :person/id)))

(pprint (search-home-by-person-id (d/db init/conn) (-> home :home/person :person/id)))
(pprint (search-home-by-person-id-2 (d/db init/conn) (-> home :home/person :person/id)))
;
(time (dotimes [_ 1000] (lookup-by-person-id (d/db init/conn) (-> home :home/person :person/id)))) ; 1o
(time (dotimes [_ 1000] (lookup-by-person-id-2 (d/db init/conn) (-> home :home/person :person/id)))) ; 2o (prox do 1o)
(time (dotimes [_ 1000] (lookup-by-person-id-3 (d/db init/conn) (-> home :home/person :person/id)))) ; pior de todos
(time (dotimes [_ 1000] (search-home-by-person-id (d/db init/conn) (-> home :home/person :person/id)))) ;3o
(time (dotimes [_ 1000] (search-home-by-person-id-2 (d/db init/conn) (-> home :home/person :person/id)))) ;4o



