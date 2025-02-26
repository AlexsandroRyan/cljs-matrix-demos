(ns demo.matrix.room
  (:require [cljs.core.async :refer [go]]
            [cljs.core.async.interop :refer-macros [<p!]]))

(defn create-room
  "Creates a room as the inviter and invites the given invitee.
   Returns the room id."
  [inviter-client invitee-user-id]
  (go
    (let [room-res (<p! (.createRoom inviter-client
                                     #js {"invite" #js [invitee-user-id]
                                          "preset" "trusted_private_chat"
                                          "is_direct" true}))
          room-id (.-room_id room-res)]
      (println "Room created:" room-id)
      room-id)))

(defn enable-encryption
  "Enables encryption in the specified room."
  [client room-id]
  (go
    (let [encryption-content #js {:algorithm "m.megolm.v1.aes-sha2"}
          _ (<p! (.sendStateEvent client room-id "m.room.encryption" encryption-content ""))]
      (println "Encryption enabled for room:" room-id)
      room-id)))

(defn join-room
  "Makes the client join the specified room."
  [client room-id]
  (go
    (let [join-res (<p! (.joinRoom client room-id))]
      (println "Room joined:" room-id)
      join-res)))

(defn update-direct-message
  "Updates the m.direct account data for the given client to mark room-id as a DM with user-id."
  [client user-id room-id]
  (go
    (let [direct-data (clj->js {user-id [room-id]})]
      (<p! (.setAccountData client "m.direct" direct-data))
      (println "Updated direct messaging data for" user-id))))

(defn send-test-message
  "Sends a text message with the given body to room-id."
  [client room-id message]
  (go
    (let [content #js {:msgtype "m.text" :body message}]
      (<p! (.sendEvent client room-id "m.room.message" content ""))
      (println "Message sent by client in room" room-id))))

