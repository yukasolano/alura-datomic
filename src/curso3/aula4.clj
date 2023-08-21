(ns curso3.aula4
  (:use clojure.pprint)
  (:require [datomic.api :as d]
            [curso3.db :as db]
            [curso3.sample :as sample]
            [schema.core :as s]))

; Curso 3 - Datomic: Schemas e Regras
; Aula 4 - Finds specs

; Follow readme to run datomic transactor

;Open db conection with transactor datomic:dev://localhost:4334/hello
(def conn (db/abre-conexao))

;Create schemas
(db/cria-schema conn)

(s/set-fn-validation! true)

;Add sample data
(sample/adiciona-dados conn)

(println "All products")
(def produtos (db/todos-os-produtos (d/db conn)))
(pprint produtos)

(println "All product with stock")
(pprint (db/todos-os-produtos-com-estoque (d/db conn)))
(println "Primeiro")
(pprint (first produtos))
(println "Segundo")
(pprint (second produtos))

(println "Returns a product if it has stock: returns first")
(pprint (db/um-produto-com-estoque (d/db conn) (:produto/id (first produtos))))

(println "Returns a product if it has stock: second has no stock")
(pprint (db/um-produto-com-estoque (d/db conn) (:produto/id (second produtos))))

; Delete db
(db/apaga-banco)
