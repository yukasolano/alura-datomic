(ns curso1.model)

(defn novo-produto [nome slug preco]
  {:produto/nome nome
   :produto/slug slug
   :produto/preco preco})