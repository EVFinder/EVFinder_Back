package com.example.backend.payment.util;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.SetOptions;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ExecutionException;

@Component
public class PaymentUtil {

    private final Firestore firestore;

    public PaymentUtil(Firestore firestore) {
        this.firestore = firestore;
    }

    public void savePayment(String uid, Map<String, Object> paymentData)
            throws ExecutionException, InterruptedException {
        firestore.collection("users")
                .document(uid)
                .collection("payments")
                .document((String) paymentData.getOrDefault("paymentId", "unknown"))
                .set(paymentData, SetOptions.merge())
                .get();
    }

    public boolean isUserExists(String uid) throws ExecutionException, InterruptedException {
        DocumentSnapshot snapshot = firestore.collection("users")
                .document(uid)
                .get()
                .get();
        return snapshot.exists();
    }

    public Firestore getFirestore() {
        return firestore;
    }
}
