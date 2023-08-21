(ns curso5.aula1
  (:use clojure.pprint)
  (:require [datomic.api :as d]
            [curso5.db.config :as db.config]
            [curso5.db.produto :as db.produto]
            [curso5.sample :as sample]
            [schema.core :as s]))

; Curso 5 - Datomic: banco filtrado e histórico
; Aula 1 - Organização

; Follow readme to run datomic transactor

;Open db conection with transactor datomic:dev://localhost:4334/ecommerce
(def conn (db.config/abre-conexao))

;Create schemas
(db.config/cria-schema conn)

(s/set-fn-validation! true)

;Add sample data
(sample/adiciona-dados conn)

;Get all products
(def produtos (db.produto/get-all (d/db conn)))

;Get first product
(println "First product")
(def primeiro (first produtos))
(pprint primeiro)


; Delete db
(db.config/apaga-banco)
