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

### 🛠️ API Reference & Usage

#### 1. Initialization & Connection
```kotlin
val playBilling = PlayBilling.create(context, debugEnabled = true)

// Connect to Google Play
playBilling.connect()

// Observe connection status
lifecycleScope.launch {
    playBilling.isReady.collect { ready ->
        if (ready) println("Billing service connected")
    }
}
```

#### 2. Observe Purchases
The `purchases` flow emits the updated list whenever a purchase is made, acknowledged, or refreshed.
```kotlin
lifecycleScope.launch {
    playBilling.purchases.collect { purchases ->
        purchases.forEach { purchase ->
            if (purchase.isAcknowledged) {
                // Grant access to content
            }
        }
    }
}
```

#### 3. Query and Buy Products
```kotlin
// Fetch products (SUBS or INAPP)
val products = playBilling.getProducts(
    productIds = listOf("premium_sub_monthly"),
    type = BillingClient.ProductType.SUBS
)

// Launch purchase flow
products.firstOrNull()?.let { product ->
    playBilling.launchPurchase(
        activity = activity,
        productId = product.productId,
        type = product.type,
        offerToken = null // Optional: specifically for subscriptions
    )
}
```

#### 4. Refresh and Cleanup
```kotlin
// Manually refresh purchases (e.g., in onResume)
playBilling.refreshPurchases()

// Clean up when done (usually in ViewModel.onCleared or Activity.onDestroy)
playBilling.destroy()
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

### 🛠️ Referencia de API y Uso

#### 1. Inicialización y Conexión
```kotlin
val playBilling = PlayBilling.create(context, debugEnabled = true)

// Conectar a Google Play
playBilling.connect()

// Observar estado de la conexión
lifecycleScope.launch {
    playBilling.isReady.collect { ready ->
        if (ready) println("Servicio de facturación conectado")
    }
}
```

#### 2. Observar Compras
El flujo `purchases` emite la lista actualizada cada vez que se realiza, confirma o actualiza una compra.
```kotlin
lifecycleScope.launch {
    playBilling.purchases.collect { purchases ->
        purchases.forEach { purchase ->
            if (purchase.isAcknowledged) {
                // Otorgar acceso al contenido
            }
        }
    }
}
```

#### 3. Consultar y Comprar Productos
```kotlin
// Consultar productos (SUBS o INAPP)
val products = playBilling.getProducts(
    productIds = listOf("premium_sub_monthly"),
    type = BillingClient.ProductType.SUBS
)

// Lanzar flujo de compra
products.firstOrNull()?.let { product ->
    playBilling.launchPurchase(
        activity = activity,
        productId = product.productId,
        type = product.type,
        offerToken = null // Opcional: específico para suscripciones
    )
}
```

#### 4. Actualizar y Limpiar
```kotlin
// Actualizar compras manualmente (ej: en onResume)
playBilling.refreshPurchases()

// Limpiar al terminar (generalmente en ViewModel.onCleared o Activity.onDestroy)
playBilling.destroy()
```
