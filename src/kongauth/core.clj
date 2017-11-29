(ns kongauth.core
  (:gen-class)
  (:require [system.repl :refer [set-init! start]]
            [kongauth.systems :as system]))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (set-init! #'system/prod-system)
  (start))
