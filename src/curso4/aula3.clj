(ns curso4.aula3
  (:use clojure.pprint)
  (:require [datomic.api :as d]
            [curso4.db :as db]
            [curso4.sample :as sample]
            [schema.core :as s]))

; Curso 4 - Datomic: Bindings, transaction functions e filters
; Aula 3 - Components e mais functions

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
(def primeiro (first produtos))

(println "Add variacao 'season pass' to first product")
(pprint @(db/adiciona-variacao! conn (:produto/id primeiro) "Season pass" 40M))
(println "Add variacao 'season pass 4 anos' to first product")
(pprint @(db/adiciona-variacao! conn (:produto/id primeiro) "Season pass 4 anos" 60M))

(println "All products")
(pprint (db/todos-os-produtos (d/db conn)))

; Component is something that exist only for that entity
; variacao does not exist without a product
; so when use pull, it get all variacao associated with
; schema need to defined as :db/isComponent true
(println "All product without pull specifyng variacao")
(pprint (d/q '[:find (pull ?produto [*])
               :where [?produto :produto/nome]]
             (d/db conn)))

(println "Total de produtos:" (db/total-de-produtos (d/db conn)))
(println "Variacoes antes do remove")
(pprint (d/q '[:find ?nome
               :where [_ :variacao/nome ?nome]]
             (d/db conn)))
(println "Remove primeiro produto")
(pprint @(db/remove-produto! conn (:produto/id primeiro)))
(println "Total de produtos:" (db/total-de-produtos (d/db conn)))

(println "Variacoes após o remove: o remove apagou também as variações por causa do component")
(pprint (d/q '[:find ?nome
               :where [_ :variacao/nome ?nome]]
             (d/db conn)))

; Delete db
(db/apaga-banco)
