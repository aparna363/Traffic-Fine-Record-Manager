
package com.example.trafficfinemanager;

import android.app.Activity;
import android.app.AlertDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.Locale;

public class MainActivity extends Activity {

    private EditText fineId;
    private EditText vehicleNumber;
    private EditText fineAmount;
    private EditText fineDate;
    private Spinner violationType;
    private RadioGroup paymentStatus;

    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fineId = findViewById(R.id.fineId);
        vehicleNumber = findViewById(R.id.vehicleNumber);
        violationType = findViewById(R.id.violationType);
        fineAmount = findViewById(R.id.fineAmount);
        fineDate = findViewById(R.id.fineDate);
        paymentStatus = findViewById(R.id.paymentStatus);

        Button saveButton = findViewById(R.id.saveButton);
        Button viewButton = findViewById(R.id.viewButton);
        Button loadButton = findViewById(R.id.loadButton);
        Button updateButton = findViewById(R.id.updateButton);
        Button clearButton = findViewById(R.id.clearButton);

        dbHelper = new DBHelper(this);

        String[] violations = {
                "Speeding",
                "No Helmet",
                "No Seat Belt",
                "Signal Jump",
                "Wrong Parking",
                "Driving Without License"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                violations
        );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        violationType.setAdapter(adapter);
        paymentStatus.check(R.id.pending);

        saveButton.setOnClickListener(v -> saveFine());
        viewButton.setOnClickListener(v -> viewFines());
        loadButton.setOnClickListener(v -> loadFineForUpdate());
        updateButton.setOnClickListener(v -> updateFine());
        clearButton.setOnClickListener(v -> clearFields());
    }

    private boolean validateFineFields() {
        if (vehicleNumber.getText().toString().trim().isEmpty()) {
            vehicleNumber.setError("Enter vehicle number");
            vehicleNumber.requestFocus();
            return false;
        }

        if (fineAmount.getText().toString().trim().isEmpty()) {
            fineAmount.setError("Enter fine amount");
            fineAmount.requestFocus();
            return false;
        }

        if (fineDate.getText().toString().trim().isEmpty()) {
            fineDate.setError("Enter fine date");
            fineDate.requestFocus();
            return false;
        }

        try {
            double amount = Double.parseDouble(
                    fineAmount.getText().toString().trim());

            if (!Double.isFinite(amount) || amount <= 0) {
                fineAmount.setError("Amount must be greater than zero");
                fineAmount.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            fineAmount.setError("Enter a valid amount");
            fineAmount.requestFocus();
            return false;
        }

        if (paymentStatus.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, "Select payment status",
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private String getSelectedStatus() {
        return paymentStatus.getCheckedRadioButtonId() == R.id.paid
                ? "Paid" : "Pending";
    }

    private double getAmount() {
        return Double.parseDouble(
                fineAmount.getText().toString().trim());
    }

    private void saveFine() {
        if (!validateFineFields()) {
            return;
        }

        boolean inserted = dbHelper.insertFine(
                vehicleNumber.getText().toString()
                        .trim().toUpperCase(Locale.ROOT),
                violationType.getSelectedItem().toString(),
                getAmount(),
                fineDate.getText().toString().trim(),
                getSelectedStatus()
        );

        if (inserted) {
            Toast.makeText(this, "Fine saved successfully!",
                    Toast.LENGTH_SHORT).show();
            clearFields();
        } else {
            Toast.makeText(this, "Unable to save fine",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void viewFines() {
        Cursor cursor = dbHelper.getAllFines();

        try {
            if (cursor == null || !cursor.moveToFirst()) {
                new AlertDialog.Builder(this)
                        .setTitle("Traffic Fine Records")
                        .setMessage("No fine records found.")
                        .setPositiveButton("OK", null)
                        .show();
                return;
            }

            StringBuilder records = new StringBuilder();

            do {
                int id = cursor.getInt(
                        cursor.getColumnIndexOrThrow(DBHelper.COL_ID));
                String vehicle = cursor.getString(
                        cursor.getColumnIndexOrThrow(DBHelper.COL_VEHICLE));
                String violation = cursor.getString(
                        cursor.getColumnIndexOrThrow(DBHelper.COL_VIOLATION));
                double amount = cursor.getDouble(
                        cursor.getColumnIndexOrThrow(DBHelper.COL_AMOUNT));
                String date = cursor.getString(
                        cursor.getColumnIndexOrThrow(DBHelper.COL_DATE));
                String status = cursor.getString(
                        cursor.getColumnIndexOrThrow(DBHelper.COL_STATUS));

                records.append("Fine ID: ").append(id)
                        .append("\nVehicle: ").append(vehicle)
                        .append("\nViolation: ").append(violation)
                        .append(String.format(Locale.getDefault(),
                                "\nAmount: ₹%.2f", amount))
                        .append("\nDate: ").append(date)
                        .append("\nPayment: ").append(status)
                        .append("\n\n----------------------\n\n");

            } while (cursor.moveToNext());

            new AlertDialog.Builder(this)
                    .setTitle("All Traffic Fine Records")
                    .setMessage(records.toString())
                    .setPositiveButton("Close", null)
                    .show();

        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void loadFineForUpdate() {
        String idText = fineId.getText().toString().trim();

        if (idText.isEmpty()) {
            fineId.setError("Enter Fine ID");
            fineId.requestFocus();
            return;
        }

        int id;

        try {
            id = Integer.parseInt(idText);
        } catch (NumberFormatException e) {
            fineId.setError("Enter a valid Fine ID");
            return;
        }

        if (id <= 0) {
            fineId.setError("Fine ID must be positive");
            return;
        }

        Cursor cursor = dbHelper.getFineById(id);

        try {
            if (cursor != null && cursor.moveToFirst()) {
                vehicleNumber.setText(cursor.getString(
                        cursor.getColumnIndexOrThrow(
                                DBHelper.COL_VEHICLE)));

                String violation = cursor.getString(
                        cursor.getColumnIndexOrThrow(
                                DBHelper.COL_VIOLATION));

                for (int i = 0; i < violationType.getCount(); i++) {
                    if (violationType.getItemAtPosition(i)
                            .toString().equals(violation)) {
                        violationType.setSelection(i);
                        break;
                    }
                }

                fineAmount.setText(String.valueOf(cursor.getDouble(
                        cursor.getColumnIndexOrThrow(
                                DBHelper.COL_AMOUNT))));

                fineDate.setText(cursor.getString(
                        cursor.getColumnIndexOrThrow(
                                DBHelper.COL_DATE)));

                String status = cursor.getString(
                        cursor.getColumnIndexOrThrow(
                                DBHelper.COL_STATUS));

                paymentStatus.check(
                        "Paid".equalsIgnoreCase(status)
                                ? R.id.paid : R.id.pending
                );

                Toast.makeText(this,
                        "Fine loaded. Edit details and tap Update Fine.",
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Fine ID not found",
                        Toast.LENGTH_SHORT).show();
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void updateFine() {
        String idText = fineId.getText().toString().trim();

        if (idText.isEmpty()) {
            fineId.setError("Enter Fine ID");
            return;
        }

        int id;

        try {
            id = Integer.parseInt(idText);
        } catch (NumberFormatException e) {
            fineId.setError("Enter a valid Fine ID");
            return;
        }

        if (id <= 0) {
            fineId.setError("Fine ID must be positive");
            return;
        }

        if (!validateFineFields()) {
            return;
        }

        boolean updated = dbHelper.updateFine(
                id,
                vehicleNumber.getText().toString()
                        .trim().toUpperCase(Locale.ROOT),
                violationType.getSelectedItem().toString(),
                getAmount(),
                fineDate.getText().toString().trim(),
                getSelectedStatus()
        );

        if (updated) {
            Toast.makeText(this, "Fine updated successfully!",
                    Toast.LENGTH_SHORT).show();
            clearFields();
        } else {
            Toast.makeText(this,
                    "Fine ID not found or no changes made",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void clearFields() {
        fineId.setText("");
        vehicleNumber.setText("");
        fineAmount.setText("");
        fineDate.setText("");

        violationType.setSelection(0);
        paymentStatus.check(R.id.pending);

        fineId.setError(null);
        vehicleNumber.setError(null);
        fineAmount.setError(null);
        fineDate.setError(null);

        vehicleNumber.requestFocus();
    }

    @Override
    protected void onDestroy() {
        if (dbHelper != null) {
            dbHelper.close();
        }
        super.onDestroy();
    }
}
