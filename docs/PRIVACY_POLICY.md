# Privacy Policy for Hermes CX

**Effective Date:** November 9, 2026  
**Application Name:** Hermes CX (Currency & Unit Converter)

## 1. Overview
Hermes CX ("the App") is a currency and unit converter application for Android. We respect your privacy and are committed to protecting it. The App is designed to operate primarily on your device and does not collect, store, sell, or share any personal information.

## 2. Information We Collect

### a. Personal Information
We **do not** collect, request, or store any personally identifiable information (PII) such as your name, email address, phone number, physical address, account credentials, contacts, or precise location. No user account or sign-in is required to use the App.

### b. Local Device Storage
To provide a seamless and offline-capable experience, the App stores the following non-personal configuration data exclusively on your device using Android local storage (`SharedPreferences` and an on-device SQLite/Room database):
- Cached currency exchange rates and last-sync timestamps for offline calculations
- Pinned currencies and saved currency pairs
- Selected unit conversion categories and unit pairs
- Display and calculation preferences (theme mode, accent color palette, decimal precision, optional bank/transfer fee percentage, and rate refresh frequency)

This data remains strictly on your device and is never transmitted to us or any third party. You can clear all locally stored data at any time through your device's Android System Settings (**Settings > Apps > Hermes CX > Storage & cache > Clear storage**) or by uninstalling the App.

## 3. Permissions Used
The App requests only the minimum technical permissions necessary to function:
- **`android.permission.INTERNET`**: Used solely to fetch the latest public foreign exchange rates over secure HTTPS connections.
- **`android.permission.ACCESS_NETWORK_STATE`**: Used to check whether your device has an active network connection so the App can seamlessly switch to cached offline rates when you are offline.

## 4. Third-Party Services
To provide up-to-date currency exchange rates, the App makes read-only HTTPS requests to the public **ExchangeRate-API** (`https://open.er-api.com/`). When the App requests updated currency rates:
- Only the requested currency code is sent in the API endpoint URL.
- Standard network metadata (such as your IP address and standard HTTP request headers) is processed by the API provider solely to deliver the response over the internet.
- The App does not include any third-party advertising SDKs, analytics trackers, or crash-reporting profilers.

## 5. Children's Privacy
The App does not knowingly collect any personal information from anyone, including children under the age of 13. Because no personal data is collected or transmitted, the App is safe for general audiences.

## 6. Data Security
All network requests for public exchange rate updates are transmitted over encrypted HTTPS connections, and all user preferences are stored locally within Android's sandboxed application storage.

## 7. Changes to This Privacy Policy
We may update this Privacy Policy from time to time to reflect changes to the App or legal requirements. Any updates will be reflected with a revised "Effective Date" at the top of this policy and within the App.

## 8. Contact Us
If you have any questions or concerns about this Privacy Policy or the App's privacy practices, please contact the developer at:
- **Email:** teohpix@gmail.com
