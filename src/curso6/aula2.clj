(ns curso6.aula2
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
; Aula 2 - Gerando produtos

; Follow readme to run datomic transactor

;Open db conection with transactor datomic:dev://localhost:4334/ecommerce
(db.config/apaga-banco)
(def conn (db.config/abre-conexao))

;Create schemas
(db.config/cria-schema conn)

(s/set-fn-validation! true)

;Add sample data
(sample/adiciona-dados conn)

;Get all products
(def produtos (db.produto/get-all (d/db conn)))


; Generates 10 Categoria
(pprint (g/sample 10 model/Categoria))

;Generates 10 Variacao com gerador customizado
(pprint (g/sample 10 model/Variacao generators/leaf-generators))

;Generates 10 Variacao com gerador customizado
(def produtos-gerados (g/sample 10 model/Produto generators/leaf-generators))
(pprint produtos-gerados)

;Adiciona no banco
(pprint (count @(db.produto/add conn produtos-gerados)))
(pprint (count (db.produto/get-all (d/db conn))))

(defn gera-10000-produtos [conn]
  (dotimes [atual 50]
    (def produtos-gerados (g/sample 200 model/Produto generators/leaf-generators))
    (println atual (count @(db.produto/add conn produtos-gerados)))))

(println "Levou o tempo a seguir para gerar 10000 produtos")
(time (gera-10000-produtos conn))

(println "Levou o tempo a seguir para achar o produto mais caro:")
(time (dotimes [_ 100] (db.produto/busca-mais-caro (d/db conn))))

(println "Levou o tempo a seguir para achar quantos produtos mais caros que 50000:")
(time (dotimes [_ 100] (count (db.produto/busca-mais-caro-que (d/db conn) 50000M))))

; Delete db
;(db.config/apaga-banco)
