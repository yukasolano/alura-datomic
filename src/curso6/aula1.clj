(ns curso6.aula1
  (:use clojure.pprint)
  (:require [datomic.api :as d]
            [curso6.db.config :as db.config]
            [curso6.db.produto :as db.produto]
            [curso6.sample :as sample]
            [schema.core :as s]
            [schema-generators.generators :as g]
            [curso6.model :as model]
            [curso6.generators :as generators]))

; Curso 6 - Datomic: geradores, schemas e índices
; Aula 1 - Leaf generators

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


; Generates 100 Categoria
(pprint (g/sample 10 model/Categoria))

;Generates 1 Variacao com gerador customizado
(pprint (g/sample 10 model/Variacao generators/leaf-generators))


; Delete db
(db.config/apaga-banco)
