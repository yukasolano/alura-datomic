(ns curso3.aula3
  (:use clojure.pprint)
  (:require [datomic.api :as d]
            [curso3.db :as db]
            [curso3.model :as model]
            [curso3.sample :as sample]
            [schema.core :as s]))

; Curso 3 - Datomic: Schemas e Regras
; Aula 3 - Maybe e optional key

; Follow readme to run datomic transactor

;Open db conection with transactor datomic:dev://localhost:4334/hello
(def conn (db/abre-conexao))

;Create schemas
(db/cria-schema conn)

(s/set-fn-validation! true)

;Add sample data
(sample/adiciona-dados conn)

;All products
(def produtos (db/todos-os-produtos (d/db conn)))
(def primeiro-produto (first produtos))
(println "First product")
(pprint primeiro-produto)

;qndo nao existe devolve nil
(println "Returns nil when product does not exist")
(pprint (db/um-produto (d/db conn) (:produto/id primeiro-produto)))
(pprint (db/um-produto (d/db conn) (model/uuid)))

;qndo nao existe lança uma exceção
(println "Throws exception when product does not exist")
(pprint (db/um-produto! (d/db conn) (:produto/id primeiro-produto)))
(pprint (db/um-produto! (d/db conn) (model/uuid)))

; Delete db
(db/apaga-banco)
