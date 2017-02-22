(ns tictag.server-chimer
  (:require [taoensso.timbre :as timbre]
            [com.stuartsierra.component :as component]
            [tictag.db :as db]
            [chime :refer [chime-at]]
            [clj-time.coerce :as tc]
            [taoensso.timbre :as timbre]
            [tictag.twilio :as twilio]
            [tictag.slack :as slack]))

(defrecord ServerChimer [db twilio slack]
  component/Lifecycle
  (start [component]
    (timbre/debug "Starting server chimer (for twilio sms)")
    (let [state (atom (cycle (shuffle (range 1000))))
          next! (fn [] (swap! state next) (str (first @state)))]
      (assoc
       component
       :stop
       (chime-at
        (db/pings db)
        (fn [time]
          (let [long-time (tc/to-long time)
                id        (next!)]
            (timbre/debug "CHIME!")
            (db/add-pending! db long-time id)
            (slack/send-message! slack (format "PING! id: %s, long-time: %d" id long-time))
            (twilio/send-message!
             twilio
             (format "PING! id: %s, long-time: %d" id long-time))))))))
  (stop [component]
    (when-let [stop (:stop component)]
      (stop))

    (dissoc component :stop)))
