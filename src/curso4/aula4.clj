(ns curso4.aula4
  (:use clojure.pprint)
  (:require [datomic.api :as d]
            [curso4.db :as db]
            [curso4.sample :as sample]
            [schema.core :as s]))

; Curso 4 - Datomic: Bindings, transaction functions e filters
; Aula 4 - History

; Follow readme to run datomic transactor

;Open db conection with transactor datomic:dev://localhost:4334/hello
(def conn (db/abre-conexao))

;Create schemas
(db/cria-schema conn)

(s/set-fn-validation! true)

;Add sample data
(sample/adiciona-dados conn)

;Get all products
(def produtos (db/todos-os-produtos (d/db conn)))

;Get first product
(println "First product")
(def primeiro (first produtos))
(pprint primeiro)

(println "Add 4 visualizacoes to first product")
(pprint  @(db/adiciona-visualizacao conn (:produto/id primeiro)))
(pprint  @(db/adiciona-visualizacao conn (:produto/id primeiro)))
(pprint  @(db/adiciona-visualizacao conn (:produto/id primeiro)))
(pprint  @(db/adiciona-visualizacao conn (:produto/id primeiro)))

(println "First product with view")
(pprint (db/um-produto (d/db conn) (:produto/id primeiro)))


; Delete db
(db/apaga-banco)
