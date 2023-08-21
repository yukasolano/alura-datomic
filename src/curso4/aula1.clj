(ns curso4.aula1
  (:use clojure.pprint)
  (:require [datomic.api :as d]
            [curso4.db :as db]
            [curso4.sample :as sample]
            [schema.core :as s]))

; Curso 4 - Datomic: Bindings, transaction functions e filters
; Aula 1 - Bindings

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


(println "Todos os produtos nas categorias Eletrônicos e Alimentação")
(pprint (db/todos-os-produtos-nas-categorias (d/db conn) ["Eletrônicos" "Alimentação"]))
(println "Todos os produtos nas categorias Eletrônicos e Esporte")
(pprint (db/todos-os-produtos-nas-categorias (d/db conn) ["Eletrônicos" "Esporte"]))
(println "Todos os produtos nas categorias Esporte")
(pprint (db/todos-os-produtos-nas-categorias (d/db conn) ["Esporte"]))
(println "Todos os produtos na categoria com lista vazia")
(pprint (db/todos-os-produtos-nas-categorias (d/db conn) []))
(println "Todos os produtos na categoria Alimentação")
(pprint (db/todos-os-produtos-nas-categorias (d/db conn) ["Alimentação"]))

(println "Todos os produtos digitais na categoria Eletrônicos")
(pprint (db/todos-os-produtos-nas-categorias-e-digital (d/db conn) ["Eletrônicos"] true))
(println "Todos os produtos que não são digitais na categoria Eletrônico")
(pprint (db/todos-os-produtos-nas-categorias-e-digital (d/db conn) ["Eletrônicos"] false))

; Delete db
(db/apaga-banco)
