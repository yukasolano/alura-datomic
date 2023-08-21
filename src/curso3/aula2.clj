(ns curso3.aula2
  (:use clojure.pprint)
  (:require [datomic.api :as d]
            [curso3.db :as db]
            [curso3.model :as model]
            [curso3.sample :as sample]
            [schema.core :as s]))

; Curso 3 - Datomic: Schemas e Regras
; Aula 2 - Upsert e problemas

; Follow readme to run datomic transactor

;Open db conection with transactor datomic:dev://localhost:4334/hello
(def conn (db/abre-conexao))

;Create schemas
(db/cria-schema conn)

(s/set-fn-validation! true)

;Add sample data
(sample/adiciona-dados conn)


(def dama {:produto/nome  "Dama"
           :produto/slug  "/dama"
           :produto/preco 15M
           :produto/id    (model/uuid)})

; Here we are updating the whole product
(println "Create dama product")
(db/adiciona-produtos conn [dama])
(pprint (db/um-produto (d/db conn) (:produto/id dama)))

(println "Add slug to dama product")
(db/adiciona-produtos conn [(assoc dama :produto/slug "/jogo-de-dama")])
(pprint (db/um-produto (d/db conn) (:produto/id dama)))

(println "Change price of dama product")
(db/adiciona-produtos conn [(assoc dama :produto/preco 150M)])
(pprint (db/um-produto (d/db conn) (:produto/id dama)))
;------

;Here we are updating the product partially
(defn atualiza-preco []
  (println "Atualizando preco ...")
  (let [produto {:produto/id (:produto/id dama), :produto/preco 990M}]
    (db/adiciona-produtos conn [produto])
    (println "Preco atualizado")
    produto))

(defn atualiza-slug []
  (println "Atualizando slug ...")
  (let [produto {:produto/id (:produto/id dama), :produto/slug "/jogo-de-dama-carinho"}]
    (db/adiciona-produtos conn [produto])
    (println "Slug atualizado")
    produto))

(defn roda-transacoes [tx]
  (let [futuros (mapv #(future (%)) tx)]
    (pprint (map deref futuros))
    (println "Resultado final")
    (pprint (db/um-produto (d/db conn) (:produto/id dama)))))

(roda-transacoes [atualiza-preco atualiza-slug])
;----

; Delete db
(db/apaga-banco)
