(ns curso1.aula6
  (:use clojure.pprint)
  (:require [datomic.api :as d]
            [curso1.db :as db]
            [curso1.model :as model]))

; Curso 1 - Datomic: um banco cronológico
; Aula 6 - Otimização e cardinalidade

; Follow readme to run datomic transactor

;Open db conection with transactor datomic:dev://localhost:4334/hello
(def conn (db/abre-conexao))

;Create schemas
(db/cria-schema conn)

;Add entities
(let [computador (model/novo-produto "Computador Novo", "/computador-novo", 2500.10M)
      celular (model/novo-produto "Celular Caro", "/celular", 888888.10M)
      calculadora {:produto/nome "Calculadora com 4 operações"}
      celular-barato (model/novo-produto "Celular Barato", "/celular-barato", 0.1M)]
  (pprint @(d/transact conn [computador, celular, calculadora, celular-barato])))

(println "All products more expensive than 1000")
(pprint (db/todos-os-produtos-com-precos-acima-de (d/db conn) 1000))

(println "All products more expensive than 5000")
(pprint (db/todos-os-produtos-com-precos-acima-de (d/db conn) 5000))

(println "All products")
(pprint (db/todos-os-produtos-com-pull-tudo (d/db conn)))

(println "Add two keywords to Computador")
(d/transact conn [[:db/add 17592186045418 :produto/palavra-chave "desktop"]
                 [:db/add 17592186045418 :produto/palavra-chave "computador"]])
(pprint (db/todos-os-produtos-com-pull-tudo (d/db conn)))

(println "Remove a keyword from Computador")
(d/transact conn [[:db/retract 17592186045418 :produto/palavra-chave "computador"]])
(pprint (db/todos-os-produtos-com-pull-tudo (d/db conn)))

(println "Add a new keyword to Computador")
(d/transact conn [[:db/add 17592186045418 :produto/palavra-chave "monitor preto e branco"]])
(pprint (db/todos-os-produtos-com-pull-tudo (d/db conn)))

(println "Add keyword to Celular Caro and Celular barato")
(d/transact conn [[:db/add 17592186045419 :produto/palavra-chave "celular"]
  [:db/add 17592186045421 :produto/palavra-chave "celular"]])
(pprint (db/todos-os-produtos-com-pull-tudo (d/db conn)))

(println "All products with celular keyword")
(pprint (db/todos-os-produtos-por-palavra-chave (d/db conn) "celular"))

(println "All products with monitor preto e branco keyword")
(pprint (db/todos-os-produtos-por-palavra-chave (d/db conn) "monitor preto e branco"))

(println "All products with computador keyword")
(pprint (db/todos-os-produtos-por-palavra-chave (d/db conn) "computador"))


; Delete db
(db/apaga-banco)
