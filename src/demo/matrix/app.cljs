(ns demo.matrix.app
  (:require [demo.matrix.client :as client]
            [demo.matrix.room :as room]
            ; [cljs.core.async :refer [go <!]]
            ))

;; Login parameters for both users.
(def inviter-login-params
  #js {:type "m.login.password"
       :user "alex"
       :password "Alex@1234"})

(def invitee-login-params
  #js {:type "m.login.password"
       :user "bob"
       :password "Bob@1234"})

(defn main [& cli-args]
  ; (go
  ;   (try
  ;     ;; Log in both the inviter and invitee.
  ;     (let [inviter (<! (client/create-client inviter-login-params))
  ;           invitee (<! (client/create-client invitee-login-params))
  ;           inviter-client (:client inviter)
  ;           inviter-creds (:creds inviter)
  ;           invitee-client (:client invitee)
  ;           invitee-creds (:creds invitee)
  ;           invitee-user-id (:userId invitee-creds)]
  ;       ;; Create a room and invite the invitee.
  ;       (let [room-id (<! (room/create-room inviter-client invitee-user-id))]
  ;         ;; Enable encryption in the room.
  ;         (<! (room/enable-encryption inviter-client room-id))
  ;         ;; Have the invitee join the room.
  ;         (<! (room/join-room invitee-client room-id))
  ;         ;; Update direct message account data for both sides.
  ;         (<! (room/update-direct-message inviter-client (:userId invitee-creds) room-id))
  ;         (<! (room/update-direct-message invitee-client (:userId inviter-creds) room-id))
  ;         ;; Send a test message.
  ;         (<! (room/send-test-message inviter-client room-id "Hello from inviter!"))))
  ;     (catch js/Error e
  ;       (js/console.error "Error occurred:")
  ;       (js/console.error e))))
  )

(comment
  ;; =============================================================
  ;; Interactive Steps for Direct Encrypted Chat (Asynchronous)
  ;; =============================================================
  ;; Some steps uses cljs.core.async/take! to capture the result
  ;; and bind it to a global var for interactive inspection.

  ;; ---------------------------------
  ;; Step 1: Create the inviter client.
  (def inviter-chan (demo.matrix.client/create-client inviter-login-params))
  (cljs.core.async/take! inviter-chan
                         (fn [result]
                           (def inviter result)
                           (println "Inviter created:" (:creds result))))

  ;; ---------------------------------
  ;; Step 2: Create the invitee client.
  (def invitee-chan (demo.matrix.client/create-client invitee-login-params))
  (cljs.core.async/take! invitee-chan
                         (fn [result]
                           (def invitee result)
                           (println "Invitee created:" (:creds result))))

  ;; Make sure both inviter and invitee are bound before proceeding.
  ;; You can inspect `inviter` and `invitee` in the REPL.

  ;; ---------------------------------
  ;; Step 3: Create a room as the inviter, inviting the invitee.
  (def room-chan (demo.matrix.room/create-room (:client inviter)
                                               (:userId (:creds invitee))))
  (cljs.core.async/take! room-chan
                         (fn [r]
                           (def room-id r)
                           (println "Room created:" room-id)))

  ;; ---------------------------------
  ;; Step 4: Enable encryption in the room.
  (demo.matrix.room/enable-encryption (:client inviter) room-id)

  ;; ---------------------------------
  ;; Step 5: Have the invitee join the room.
  (demo.matrix.room/join-room (:client invitee) room-id)

  ;; ---------------------------------
  ;; Step 6: Update direct messaging account data for both sides.
  (demo.matrix.room/update-direct-message (:client inviter)
                                          (:userId (:creds invitee))
                                          room-id)

  (demo.matrix.room/update-direct-message (:client invitee)
                                          (:userId (:creds inviter))
                                          room-id)

  ;; ---------------------------------
  ;; Step 7: Send a test message from the inviter.
  (demo.matrix.room/send-test-message (:client inviter)
                                      room-id
                                      "Hello from locão!")

  (defn start-listening
    "Attaches a timeline listener to the client that prints incoming messages.
     label is a string identifier (e.g., \"Inviter\" or \"Invitee\")."
    [client label]
    (println "Attaching listener for" label)
    ;; Attach a timeline listener.
    (.on client "Room.timeline"
         (fn [event room toStartOfTimeline]
           ;; Only process live events.
           (when (and (not toStartOfTimeline)
                      (= (.getType event) "m.room.message"))
             (let [;; Use getClearContent if available (for decrypted events),
                   ;; otherwise fall back to getContent.
                   clear-content (or (when (.-getClearContent event)
                                       (.getClearContent event))
                                     (.getContent event))
                   body (.-body clear-content)]
               (println (str label " received message: " body))))))
    ;; Start the client (if not already started).
    (.startClient client)
    (println label "client started."))
  (start-listening (:client inviter) "Inviter")
  (start-listening (:client invitee) "Invitee")
  nil)

  ; Ensure the comment block returns nil.
