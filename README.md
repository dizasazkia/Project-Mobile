
# Newzee: Your Gateway to Global News

**Newzee** is a modern Android application that delivers real-time news from the News API. Designed for news enthusiasts, it offers a seamless experience with a dynamic breaking news carousel, category-based browsing, powerful search, and offline bookmarking. With a polished Material Design interface and light/dark mode support, Newzee keeps you informed in style.

## Core Features

- **Dynamic Breaking News**: Auto-scrolling carousel showcasing top headlines with smooth animations and source logos.
- **Search & Category Browsing**: Real-time search with 300ms debounce and category filters (General, Technology, Business, etc.).
- **Source Filtering**: Curate news from trusted sources like CNN, BBC, and Reuters.
- **Bookmark Management**: Save articles to a local SQLite database for offline access and manage with a single tap.
- **Intuitive UI**: Material Design with light/dark mode toggle, responsive layouts, and fluid navigation.
- **Smart Time Display**: Article timestamps formatted as "time ago" for clarity.
- **Robust Functionality**: Offline-first architecture, swipe-to-refresh, and reliable network error handling.

## Installation

### Requirements

- Android Studio (Arctic Fox or later)
- JDK 8 or higher
- Android SDK API level 21+
- Android device/emulator running Android 5.0+
- News API key from [News API](https://newsapi.org/)

### Setup

Clone the repository, open it in Android Studio, and add your News API key to `local.properties`. Sync the project with Gradle and run on a device or emulator.

## API Configuration

Newzee integrates with the News API to fetch top headlines, search results, and category-specific news (default: US). A free API key is required for access.

## How to Use

- **Home Screen**: Explore top headlines in the carousel, filter by sources (e.g., CNN, BBC), and browse additional articles.
- **Search News**: Navigate to the Search section, enter keywords or select categories like Technology or Sports.
- **Bookmark Articles**: Tap the bookmark icon to save or remove articles, accessible offline in the Bookmark section.
- **View Details**: Tap an article for full details and open the original source in a browser.
- **Toggle Theme**: Switch between light or dark themes via the home screen icon.

## Project Structure

```
- activities/
  - MainActivity
  - DetailActivity
- fragments/
  - HomeFragment
  - SearchFragment
  - BookmarkFragment
- adapters/
  - BreakingNewsAdapter
  - NewsAdapter
  - BookmarkAdapter
- models/
  - Article
  - NewsResponse
- database/
  - BookmarkDatabaseHelper
- network/
  - ApiClient
  - ApiService
- utils/
  - Constants
  - TimeUtils
  - NetworkUtils
  - FixedWormDotsIndicator
  - HorizontalMarginItemDecoration
```

## Technology Stack

### Frontend

- **Language**: Java
- **Framework**: Android Native
- **Design**: Material Design
- **Navigation**: Navigation Component
- **Image Loading**: Picasso

### Backend & Data

- **Database**: SQLite via BookmarkDatabaseHelper
- **API Client**: Retrofit 2
- **JSON Parsing**: Gson
- **Data Source**: News API

### Architecture

- **Pattern**: Fragment-based navigation
- **Storage**: Custom SQLite implementation
- **Threading**: Retrofit callbacks

## Developer

- **Name**: Diza Sazkia
- **GitHub**: [dizasazkia](https://github.com/dizasazkia)
- **Email**: dizasazkia2005@gmail.com

## Acknowledgments

- [News API](https://newsapi.org/) for reliable news data
- [Picasso](https://square.github.io/picasso/) for efficient image loading
- [Retrofit](https://square.github.io/retrofit/) for streamlined API integration
- [Material Design](https://m3.material.io/) for modern UI components
- Android developer community for inspiration and resources


_Built for those who love staying informed._
