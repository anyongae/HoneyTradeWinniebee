# Implementation Plan - E-commerce Features for WinnieBee App

This plan outlines the implementation of a full e-commerce ecosystem including user roles (Buyer/Seller), product management, escrow-style order management, and Mobile Money integration using Firebase as the backend.

## User Review Required

> [!IMPORTANT]
> **Firebase Setup:** The project currently lacks Firebase configuration. I will need to add the necessary plugins and dependencies. You will need to provide the `google-services.json` file or I can guide you through the manual setup process if you haven't created a Firebase project yet.

> [!WARNING]
> **Mobile Money Integration:** Real Mobile Money integration requires a specific provider's API (e.g., M-Pesa, Paystack, Flutterwave). This plan includes a *simulated* payment initiation flow and a structured service layer where you can plug in a real API.

## Proposed Changes

### 1. Foundation & Infrastructure
Setup the base architecture and dependencies.

#### [MODIFY] [build.gradle.kts (project)](file:///C:/Users/Emma/AndroidStudioProjects/WinnieBeeApp/build.gradle.kts)
- Add Google Services plugin.
#### [MODIFY] [build.gradle.kts (app)](file:///C:/Users/Emma/AndroidStudioProjects/WinnieBeeApp/app/build.gradle.kts)
- Add Firebase BOM, Auth, Firestore, and Storage dependencies.
- Add Navigation and Hilt (DI) dependencies.
#### [NEW] [FirebaseModule.kt](file:///C:/Users/Emma/AndroidStudioProjects/WinnieBeeApp/app/src/main/java/com/example/winniebeeapp/di/FirebaseModule.kt)
- Hilt module for providing Firebase instances.

---

### 2. Data Models & Roles
Define the core entities and role-based access.

#### [NEW] [Models.kt](file:///C:/Users/Emma/AndroidStudioProjects/WinnieBeeApp/app/src/main/java/com/example/winniebeeapp/data/model/Models.kt)
- `User` (Role: Buyer/Seller, Verification status)
- `Product` (Batch info, Verification status, Seller ID)
- `Order` (Escrow states: PENDING, PAID, SHIPPED, DELIVERED, COMPLETED, DISPUTED)
- `CartItem`

---

### 3. Authentication & Registration
Implementation of role-based login/registration.

#### [NEW] [AuthRepository.kt](file:///C:/Users/Emma/AndroidStudioProjects/WinnieBeeApp/app/src/main/java/com/example/winniebeeapp/data/repository/AuthRepository.kt)
- Firebase Auth integration for registration and login.
#### [NEW] [RegistrationScreens.kt](file:///C:/Users/Emma/AndroidStudioProjects/WinnieBeeApp/app/src/main/java/com/example/winniebeeapp/ui/auth/RegistrationScreens.kt)
- UI for Buyer and Seller registration.

---

### 4. Seller Verification & Product Management
Workflow for sellers to get verified and list products.

#### [NEW] [SellerRepository.kt](file:///C:/Users/Emma/AndroidStudioProjects/WinnieBeeApp/app/src/main/java/com/example/winniebeeapp/data/repository/SellerRepository.kt)
- Methods for seller verification requests and product batch submission.
#### [NEW] [ProductListingScreens.kt](file:///C:/Users/Emma/AndroidStudioProjects/WinnieBeeApp/app/src/main/java/com/example/winniebeeapp/ui/seller/ProductListingScreens.kt)
- Seller dashboard for listing products and managing batches.

---

### 5. Shopping Cart & Order Creation
Buyer workflow for purchasing.

#### [NEW] [CartRepository.kt](file:///C:/Users/Emma/AndroidStudioProjects/WinnieBeeApp/app/src/main/java/com/example/winniebeeapp/data/repository/CartRepository.kt)
- Local/Firestore cart management.
#### [NEW] [CartScreen.kt](file:///C:/Users/Emma/AndroidStudioProjects/WinnieBeeApp/app/src/main/java/com/example/winniebeeapp/ui/buyer/CartScreen.kt)
- Cart UI and checkout initiation.

---

### 6. Escrow & Mobile Money Payment
State-managed order flow.

#### [NEW] [OrderRepository.kt](file:///C:/Users/Emma/AndroidStudioProjects/WinnieBeeApp/app/src/main/java/com/example/winniebeeapp/data/repository/OrderRepository.kt)
- Firestore real-time updates for order state.
- Logic for Escrow-style state transitions.
#### [NEW] [PaymentService.kt](file:///C:/Users/Emma/AndroidStudioProjects/WinnieBeeApp/app/src/main/java/com/example/winniebeeapp/data/service/PaymentService.kt)
- Interface for Mobile Money initiation.

---

### 7. Tracking & Real-time Updates
Delivery status and location tracking.

#### [NEW] [TrackingRepository.kt](file:///C:/Users/Emma/AndroidStudioProjects/WinnieBeeApp/app/src/main/java/com/example/winniebeeapp/data/repository/TrackingRepository.kt)
- Firestore listeners for location and status updates.
#### [NEW] [OrderTrackingScreen.kt](file:///C:/Users/Emma/AndroidStudioProjects/WinnieBeeApp/app/src/main/java/com/example/winniebeeapp/ui/common/OrderTrackingScreen.kt)
- Real-time UI for delivery tracking.

## Verification Plan

### Automated Tests
- Unit tests for `OrderRepository` to verify escrow state transitions.
- Unit tests for `AuthRepository` mocks.

### Manual Verification
- Deploy app to an emulator.
- Register as a Seller, list a product.
- Register as a Buyer, purchase the product.
- Verify real-time updates on both dashboards when the order state changes.
