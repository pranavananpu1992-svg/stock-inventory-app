# Stock Inventory

A modern, offline-first Android stock and inventory management application built with **Kotlin**, **Jetpack Compose**, and **Room Database**, following Material Design 3 guidelines.

## Features

- **Real-Time Warehouse Dashboard**
  - Instant executive valuation metrics (Total Cost Valuation, Potential Retail Revenue, Total Units, SKU Count).
  - Low stock & out-of-stock emergency alerts banner with 1-tap reordering.
  - Interactive inventory valuation share progress bars by category.
  - Recent stock movements overview.

- **Inventory Catalog Management**
  - Complete SKU, product name, description, category, barcode, and warehouse location tracking.
  - Category filtering and instant search across SKU, name, barcode, and location.
  - One-tap quick Stock-In (+) and Stock-Out (-) adjustments directly from the item cards.
  - Comprehensive item details with gross profit margins, inventory value, and supplier contacts.

- **Barcode & SKU Scanner**
  - Interactive scanner viewfinder with animated laser alignment.
  - Instant barcode / SKU lookup with quick-test sample chips.
  - Immediate on-screen Stock-In / Stock-Out actions without leaving the scanner.

- **Warehouse Movement Audit Trail**
  - Full transactional history logging every stock in, dispatch, audit adjustment, damage/loss, and return.
  - Filter by activity type and search by item or operator.
  - Tracks balance before and after each movement with reference notes and operator names.

- **Data Export**
  - 1-tap CSV export of complete inventory catalog for spreadsheet analysis or backup.

## Tech Stack

- **UI**: Jetpack Compose & Material Design 3
- **Language**: Kotlin 2.2
- **Architecture**: MVVM with Clean Architecture & Repository pattern
- **Local Persistence**: Android Room Database with reactive Kotlin Flows
- **Build**: Gradle Kotlin DSL & Android Gradle Plugin (AGP) 9.1
