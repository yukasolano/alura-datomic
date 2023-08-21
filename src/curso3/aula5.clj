(ns curso3.aula5
  (:use clojure.pprint)
  (:require [datomic.api :as d]
            [curso3.db :as db]
            [curso3.sample :as sample]
            [schema.core :as s]))


; Curso 3 - Datomic: Schemas e Regras
; Aula 5 - Regras

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

(println "All products with stock")
(pprint (db/todos-os-produtos-com-estoque (d/db conn)))

(println "First product")
(pprint (first produtos))
(pprint (db/um-produto-com-estoque (d/db conn) (:produto/id (first produtos))))
(println "Second product")
(pprint (second produtos))
(pprint (db/um-produto-com-estoque (d/db conn) (:produto/id (second produtos))))
(println "Last product")
(pprint (last produtos))
(pprint (db/um-produto-com-estoque (d/db conn) (:produto/id (last produtos))))

(println "Analise dos produtos")
(defn verifica-se-pode-vender [produto]
  (println "Analisando produto" (:produto/nome produto))
  (println "Estoque:" (:produto/estoque produto))
  (println "É digital?" (:produto/digital produto))
  (println "Retorna produto se pode vender:")
  (pprint (db/um-produto-com-estoque (d/db conn) (:produto/id produto)))
  (println "----------------------------"))

(mapv verifica-se-pode-vender produtos)

; Delete db
(db/apaga-banco)
