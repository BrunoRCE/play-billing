# PlayBilling

[![](https://jitpack.io/v/BrunoRCE/play-billing.svg)](https://jitpack.io/#BrunoRCE/play-billing)

---

### [English 🇺🇸](#english) | [Español 🇪🇸](#español)

---

<a name="english"></a>
## English

`PlayBilling` is a modern and lightweight wrapper for the **Google Play Billing Library (v8.0.0)** designed to simplify the integration of in-app purchases and subscriptions in Android using Kotlin Coroutines and Flow.

### ✨ Features
- ✅ **Coroutine-based:** No more complex callbacks.
- ✅ **Reactive:** Observe purchase status and connection via `Flow`.
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
    implementation("com.github.BrunoRCE:play-billing:2.0.0")
}
```

### 🛠️ Basic Usage

#### Initialization
```kotlin
val playBilling = PlayBilling.create(context, debugEnabled = true)
playBilling.connect()
```

#### Observe Purchases
```kotlin
lifecycleScope.launch {
    playBilling.purchases.collect { purchases ->
        // Update your UI or business logic
        purchases.forEach { purchase ->
            println("Product purchased: ${purchase.products}")
        }
    }
}
```

#### Query Products and Launch Purchase
```kotlin
val productIds = listOf("premium_sub_monthly")
// type: BillingClient.ProductType.SUBS or BillingClient.ProductType.INAPP
val products = playBilling.getProducts(productIds, BillingClient.ProductType.SUBS)

products.firstOrNull()?.let { product ->
    playBilling.launchPurchase(
        activity = activity,
        productId = product.productId,
        type = product.type
    )
}
```

---

<a name="español"></a>
## Español

`PlayBilling` es un wrapper moderno y ligero sobre la **Google Play Billing Library (v8.0.0)** diseñado para simplificar la integración de compras in-app y suscripciones en Android utilizando Kotlin Coroutines y Flow.

### ✨ Características
- ✅ **Basado en Coroutines:** Olvídate de los callbacks complejos.
- ✅ **Reactivo:** Observa el estado de las compras y la conexión mediante `Flow`.
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
    implementation("com.github.BrunoRCE:play-billing:2.0.0")
}
```

### 🛠️ Uso Básico

#### Inicialización
```kotlin
val playBilling = PlayBilling.create(context, debugEnabled = true)
playBilling.connect()
```

#### Observar Compras
```kotlin
lifecycleScope.launch {
    playBilling.purchases.collect { purchases ->
        // Actualiza tu UI o lógica de negocio
        purchases.forEach { purchase ->
            println("Producto comprado: ${purchase.products}")
        }
    }
}
```

#### Consultar Productos y Comprar
```kotlin
val productIds = listOf("premium_sub_monthly")
// type: BillingClient.ProductType.SUBS o BillingClient.ProductType.INAPP
val products = playBilling.getProducts(productIds, BillingClient.ProductType.SUBS)

products.firstOrNull()?.let { product ->
    playBilling.launchPurchase(
        activity = activity,
        productId = product.productId,
        type = product.type
    )
}
```
