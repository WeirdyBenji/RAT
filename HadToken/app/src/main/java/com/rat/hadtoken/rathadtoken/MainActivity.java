package com.rat.hadtoken.rathadtoken;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = MainActivity.class.getSimpleName();
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    private NfcAdapter mNfcAdapter;
    private boolean inPopup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initNFC();
        inPopup = false;
    }

    private void initNFC() {
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        IntentFilter ndefDetected = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        IntentFilter techDetected = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
        IntentFilter[] nfcIntentFilter = new IntentFilter[]{techDetected, tagDetected, ndefDetected};

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        if (mNfcAdapter != null)
            mNfcAdapter.enableForegroundDispatch(this, pendingIntent, nfcIntentFilter, null);

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mNfcAdapter != null)
            mNfcAdapter.disableForegroundDispatch(this);
    }

    private String getHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte aByte : bytes) {
            int b = aByte & 0xff;
            if (b < 0x10)
                sb.append('0');
            sb.append(Integer.toHexString(b).toUpperCase());
        }

        return sb.toString();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        final Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

        if (tag != null && !inPopup) {
            inPopup = true;
            final String serialNumber = getHex(intent.getByteArrayExtra(NfcAdapter.EXTRA_ID));
            Log.d(TAG, serialNumber);

            AlertDialog.Builder mBuilder = new AlertDialog.Builder(MainActivity.this);
            View mView = getLayoutInflater().inflate(R.layout.dialogue_had_token, null);
            final EditText mEmail = mView.findViewById(R.id.editTextEmail);
            Button mButton = mView.findViewById(R.id.buttonValideEmail);

            mBuilder.setView(mView);
            mBuilder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    inPopup = false;
                }
            });
            final AlertDialog mDialog = mBuilder.create();
            mDialog.show();

            mButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!mEmail.getText().toString().isEmpty()) {
                        final String email = mEmail.getText().toString().toLowerCase();

                        db.collection("users")
                                .whereEqualTo("mail", email).get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        Map<String, String> map = new HashMap<>();

                                        if (task.isSuccessful()) {
                                            for (DocumentSnapshot doc : task.getResult()) {
                                                db.collection("users").document(doc.getId()).delete();
                                                Log.d(TAG, doc.getId() + " => " + doc.getData());
                                            }
                                        } else {
                                            Log.d(TAG, "Error getting documents: ", task.getException());
                                        }
                                        map.put("mail", email);
                                        db.collection("users").document(serialNumber).set(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                mDialog.cancel();
                                                inPopup = false;
                                                Toast.makeText(MainActivity.this, "La carte " + email + " a bien été ajouter", Toast.LENGTH_LONG).show();
                                            }
                                        });
                                    }
                                });
                    }
                }
            });

        }
    }

}
