(ns indexes.generators
  (:require [clojure.test.check.generators :as gen]))


(defn ^:private double-para-bigdecimal [valor]
  (BigDecimal. valor))

(def ^:private bigdecimal (gen/fmap
                 double-para-bigdecimal
                 (gen/double* {:infinite? false :NaN? false})))

(def leaf-generators {BigDecimal bigdecimal})
