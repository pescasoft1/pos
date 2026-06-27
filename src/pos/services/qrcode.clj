(ns pos.services.qrcode
  (:import [com.google.zxing BarcodeFormat]
           [com.google.zxing.qrcode QRCodeWriter]
           [com.google.zxing.client.j2se MatrixToImageWriter]
           [java.io ByteArrayOutputStream]
           [javax.imageio ImageIO]
           [java.util Base64]))

(defn make-product-qr-text
  [product-id]
  (str "PROD:" product-id))

(defn parse-product-qr
  [qr-text]
  (when-let [m (re-matches #"PROD:(.+)" qr-text)]
    (try
      (Long/parseLong (second m))
      (catch NumberFormatException _
        nil))))

(defn encode-to-base64
  [text opts]
  (let [size (or (:size opts) 150)
        writer (QRCodeWriter.)
        bit-matrix (.encode writer text BarcodeFormat/QR_CODE size size)
        buffered-image (MatrixToImageWriter/toBufferedImage bit-matrix)
        baos (ByteArrayOutputStream.)]
    (ImageIO/write buffered-image "png" baos)
    (str "data:image/png;base64,"
         (.encodeToString (Base64/getEncoder) (.toByteArray baos)))))
