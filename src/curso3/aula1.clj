(ns curso3.aula1
  (:use clojure.pprint)
  (:require [datomic.api :as d]
            [curso3.db :as db]
            [curso3.model :as model]
            [curso3.sample :as sample]
            [schema.core :as s]))

; Curso 3 - Datomic: Schemas e Regras
; Aula 1 - Schemas e schemas

; Follow readme to run datomic transactor

;Open db conection with transactor datomic:dev://localhost:4334/hello
(def conn (db/abre-conexao))

;Create schemas
(db/cria-schema conn)

(s/set-fn-validation! true)

(defn testa-schema []
  (let [computador (model/novo-produto (model/uuid) "Computador Novo", "/computador-novo", 2500.10M)
        eletronico (model/nova-categoria "Eletrônico")]
    (println "Validade Produto computador")
    (pprint (s/validate model/Produto computador))
    (println "Validade Categoria eletronico")
    (pprint (s/validate model/Categoria eletronico))
    (println "Validade Produto with categoria")
    (pprint (s/validate model/Produto (assoc computador :produto/categoria eletronico)))))

; Test model schemas Produto and Categoria
(testa-schema)

;Add sample data
(sample/adiciona-dados conn)

(println "All categories")
(pprint (db/todas-as-categorias (d/db conn)))

(println "All products")
(pprint (db/todos-os-produtos (d/db conn)))

; Delete db
(db/apaga-banco)

