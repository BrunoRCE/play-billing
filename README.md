# PlayBilling

[![](https://jitpack.io/v/brunorce/PlayBilling.svg)](https://jitpack.io/home/brunorce/PlayBilling)

---

### [English 🇺🇸](#english) | [Español 🇪🇸](#español)

---

<a name="english"></a>
## English

`PlayBilling` is a modern and lightweight wrapper for the **Google Play Billing Library (v7+)** designed to simplify the integration of in-app purchases and subscriptions in Android using Kotlin Coroutines and Flow.

### ✨ Features
- ✅ **Coroutine-based:** No more complex callbacks.
- ✅ **Reactive:** Observe purchase status and connection via `StateFlow`.
- ✅ **Error Handling:** Built-in automatic reconnection logic.
- ✅ **Modern Subscriptions:** Native support for multiple offers and subscription tokens.
- ✅ **Lightweight:** No unnecessary dependencies, just Kotlin and Billing Library.

### 🚀 Installation

#### 1. Add the repository (settings.gradle.kts)
```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

#### 2. Add the dependency (build.gradle.kts)
```kotlin
dependencies {
    implementation("com.github.brunorce:PlayBilling:1.0.0")
}
```

### 🛠️ Basic Usage

#### Initialization
```kotlin
val billingWrapper = PlayBillingWrapper(context, debugEnabled = true)
```

#### Observe Purchases
```kotlin
lifecycleScope.launch {
    billingWrapper.purchases.collect { purchases ->
        // Update your UI or business logic
    }
}
```

#### Query Products and Launch Purchase
```kotlin
val productIds = listOf("premium_sub_monthly")
val products = billingWrapper.getProducts(BillingClient.ProductType.SUBS, productIds)

products.firstOrNull()?.let { productDetails ->
    billingWrapper.launchPurchase(productDetails, activity)
}
```

---

<a name="español"></a>
## Español

`PlayBilling` es un wrapper moderno y ligero sobre la **Google Play Billing Library (v7+)** diseñado para simplificar la integración de compras in-app y suscripciones en Android utilizando Kotlin Coroutines y Flow.

### ✨ Características
- ✅ **Basado en Coroutines:** Olvídate de los callbacks complejos.
- ✅ **Reactivo:** Observa el estado de las compras y la conexión mediante `StateFlow`.
- ✅ **Manejo de Errores:** Lógica de reconexión automática integrada.
- ✅ **Suscripciones Modernas:** Soporte nativo para múltiples ofertas y tokens de suscripción.
- ✅ **Ligero:** Sin dependencias innecesarias, solo Kotlin y Billing Library.

### 🚀 Instalación

#### 1. Agregar el repositorio (settings.gradle.kts)
```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

#### 2. Agregar la dependencia (build.gradle.kts)
```kotlin
dependencies {
    implementation("com.github.brunorce:PlayBilling:1.0.0")
}
```

### 🛠️ Uso Básico

#### Inicialización
```kotlin
val billingWrapper = PlayBillingWrapper(context, debugEnabled = true)
```

#### Observar Compras
```kotlin
lifecycleScope.launch {
    billingWrapper.purchases.collect { purchases ->
        // Actualiza tu UI o lógica de negocio
    }
}
```

#### Consultar Productos y Comprar
```kotlin
val productIds = listOf("premium_sub_monthly")
val products = billingWrapper.getProducts(BillingClient.ProductType.SUBS, productIds)

products.firstOrNull()?.let { productDetails ->
    billingWrapper.launchPurchase(productDetails, activity)
}
```

