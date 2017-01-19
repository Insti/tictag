(ns tictag.main
  (:gen-class)
  (:require [tictag.server :as server]
            [tictag.server-api :as api]
            [tictag.client :as client]
            [tictag.client-config :as client-config]
            [tictag.utils :as utils]
            [clojure.core.async :as a :refer [<! go-loop]]
            [taoensso.timbre :as timbre] 
            [com.stuartsierra.component :as component]
            [reloaded.repl :refer [system]]
            [tictag.server-api :refer :all]))

(defn do-not-exit! []
  (a/<!!
   (go-loop []
     (let [_ (<! (a/timeout 100000))]
       (recur)))))

(defn -main [& args]
  (let [[system-type remote-url] args
        system                   (case system-type
                                   "server" server/system
                                   "client" (client/system client-config/config))]
    (reloaded.repl/set-init! (constantly (utils/system-map system)))
    (reloaded.repl/go)
    (do-not-exit!)))

