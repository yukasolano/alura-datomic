(ns curso2.db
  (:require [datomic.api :as d]))

(def db-url "datomic:dev://localhost:4334/hello")

(defn abre-conexao []
  (d/create-database db-url)
  (d/connect db-url))

(defn apaga-banco []
  (d/delete-database db-url))

(def schema [{:db/ident       :produto/nome
              :db/valueType   :db.type/string
              :db/cardinality :db.cardinality/one
              :db/doc         "O nome de um produto"}
             {:db/ident       :produto/slug
              :db/valueType   :db.type/string
              :db/cardinality :db.cardinality/one
              :db/doc         "O caminho para acessar esse produto via http"}
             {:db/ident       :produto/preco
              :db/valueType   :db.type/bigdec
              :db/cardinality :db.cardinality/one
              :db/doc         "O preço de um produto com precisão monetária"}
             {:db/ident       :produto/palavra-chave
              :db/valueType   :db.type/string
              :db/cardinality :db.cardinality/many}
             {:db/ident       :produto/id
              :db/valueType   :db.type/uuid
              :db/cardinality :db.cardinality/one
              :db/unique      :db.unique/identity}
             {:db/ident       :produto/categoria
              :db/valueType   :db.type/ref
              :db/cardinality :db.cardinality/one}

             ;categorias
             {:db/ident       :categoria/nome
              :db/valueType   :db.type/string
              :db/cardinality :db.cardinality/one}
             {:db/ident       :categoria/id
              :db/valueType   :db.type/uuid
              :db/cardinality :db.cardinality/one
              :db/unique      :db.unique/identity}

             ;transacoes
             {:db/ident       :tx-data/ip
              :db/valueType   :db.type/string
              :db/cardinality :db.cardinality/one}
             ])

(defn cria-schema [conn]
  (d/transact conn schema))

(defn todos-os-produtos [db]
  (d/q '[:find (pull ?entidade [*])
         :where [?entidade :produto/nome]] db))

(defn um-produto-por-dbid [db dbid]
  (d/pull db '[*] dbid))

(defn um-produto [db uuid]
  (d/pull db '[*] [:produto/id uuid]))

(defn adiciona-categorias [conn categorias]
  (d/transact conn categorias))

(defn adiciona-produtos
  ([conn produtos]
   (d/transact conn produtos))
  ([conn produtos ip]
   (let [db-add-ip [:db/add "datomic.tx" :tx-data/ip ip]]
     (d/transact conn (conj produtos db-add-ip)))))

(defn todas-as-categorias [db]
  (d/q '[:find (pull ?entidade [*])
         :where [?entidade :categoria/id]] db))

(defn db-adds-de-atribuicao-de-categorias [produtos categoria]
  (reduce (fn [db-adds produto]
            (conj db-adds [:db/add [:produto/id (:produto/id produto)]
                           :produto/categoria
                           [:categoria/id (:categoria/id categoria)]]))
          [] produtos))

(defn atribui-categorias [conn produtos categoria]
  (let [a-transacionar (db-adds-de-atribuicao-de-categorias produtos categoria)]
    (d/transact conn a-transacionar)))

(defn todos-os-nomes-de-produtos-e-categorias [db]
  (d/q '[:find ?nome-do-produto ?nome-da-categoria
         :keys :produto :categoria
         :where [?produto :produto/nome ?nome-do-produto]
         [?produto :produto/categoria ?categoria]
         [?categoria :categoria/nome ?nome-da-categoria]] db))

;Forward navigation
(defn todos-os-produtos-da-categoria-forward [db nome-da-categoria]
  (d/q '[:find (pull ?produto [:produto/nome :produto/slug {:produto/categoria [:categoria/nome]}])
         :in $ ?nome-da-categoria
         :where [?categoria :categoria/nome ?nome-da-categoria]
         [?produto :produto/categoria ?categoria]]
       db nome-da-categoria))

;Backward navigation
(defn todos-os-produtos-da-categoria-backward [db nome-da-categoria]
  (d/q '[:find (pull ?categoria [:categoria/nome {:produto/_categoria [:produto/nome :produto/slug]}])
         :in $ ?nome-da-categoria
         :where [?categoria :categoria/nome ?nome-da-categoria]]
       db nome-da-categoria))

(defn resumo-dos-produtos [db]
  (d/q '[:find (min ?preco) (max ?preco) (count ?preco) (sum ?preco)
         :keys minimo maximo quantidade total
         :with ?produto
         :where [?produto :produto/preco ?preco]] db))

(defn resumo-dos-produtos-por-categoria [db]
  (d/q '[:find ?nome-da-categoria (min ?preco) (max ?preco) (count ?preco) (sum ?preco)
         :keys categoria minimo maximo quantidade total
         :with ?produto
         :where [?produto :produto/preco ?preco]
         [?produto :produto/categoria ?categoria]
         [?categoria :categoria/nome ?nome-da-categoria]] db))


(defn todos-os-produtos-mais-caros [db]
  (d/q '[:find (pull ?produto [*])
         :where [(q '[:find (max ?preco)
                      :where [_ :produto/preco ?preco]]
                    $) [[?preco]]]                  ;extract preco da nested query
         [?produto :produto/preco ?preco]] db))

(defn todos-os-produtos-mais-baratos [db]
  (d/q '[:find (pull ?produto [*])
         :where [(q '[:find (min ?preco)
                      :where [_ :produto/preco ?preco]]
                    $) [[?preco]]]
         [?produto :produto/preco ?preco]] db))

(defn todos-os-produtos-do-ip [db ip]
  (d/q '[:find (pull ?produto [*])
         :in $ ?ip
         :where [?transacao :tx-data/ip ?ip]
         [?produto :produto/id _ ?transacao]]
       db ip))
