(ns demo.matrix.client
  (:require ["matrix-js-sdk" :as matrix.sdk]
            [cljs.core.async :refer [go]]
            [cljs.core.async.interop :refer-macros [<p!]]
            [clojure.set :as clj.set]))

(def base-url "http://localhost:8008")

(defn create-client
  "Logs in a user given login parameters, initializes crypto if available,
   and returns a map with the client and credentials."
  [login-params]
  (go
    (let [client-params #js {:baseUrl base-url}
          tmp-client (matrix.sdk/createClient client-params)
          login-res (<p! (.loginRequest tmp-client login-params))
          ;; Massage the login response into proper keys.
          creds (-> (js->clj login-res {:keywordize-keys true})
                    (select-keys [:access_token :user_id :device_id])
                    (assoc :baseUrl base-url)
                    (clj.set/rename-keys {:access_token :accessToken
                                          :user_id :userId
                                          :device_id :deviceId}))
          client (matrix.sdk/createClient (clj->js creds))]
      (<p! (.initRustCrypto client #js {:useIndexedDB false}))
      {:client client :creds creds})))

(defn start-listening
  "Attaches a timeline listener to the client that prints incoming messages.
   label is a string identifier (e.g., \"Inviter\" or \"Invitee\")."
  [client label]
  (println "Attaching listener for" label)
  (.on client "Room.timeline"
       (fn [event]
         (go
           (when (= (.getType event) "m.room.message")
             (let [clear-content (or (when (.-getClearContent event)
                                       (.getClearContent event))
                                     (.getContent event))
                   body (.-body clear-content)]
               (println (str label " received message: " body))))
           (when (= (.getType event) "m.room.encrypted")
             (let [_ (<p! (.getDecryptionPromise event))
                   clear-content (.getClearContent event)
                   body (.-body clear-content)]
               (println (str label " received message: " body)))))))
  (.startClient client)
  (println label "client started."))
