package com.bignerdranch.android.mybank

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.util.Locale
import java.math.BigDecimal

private val Navy = Color(0xFF142D4E)
private val BankBlue = Color(0xFF245A91)
private val LightBackground = Color(0xFFF2F5F9)
private val DepositGreen = Color(0xFF21834A)

data class Transaction(
    val name: String,
    val amount: Long
)

fun money(cents: Long): String {
    return NumberFormat.getCurrencyInstance(Locale.US)
        .format(BigDecimal.valueOf(cents, 2))
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MyBankApp()
        }
    }
}

// Main application

@Composable
fun MyBankApp() {

    var loggedIn by rememberSaveable {
        mutableStateOf(false)
    }

    var currentScreen by rememberSaveable {
        mutableStateOf("Home")
    }

    var checkingBalance by rememberSaveable {
        mutableLongStateOf(598798L)
    }

    var savingsBalance by rememberSaveable {
        mutableLongStateOf(250000L)
    }

    var darkMode by rememberSaveable {
        mutableStateOf(false)
    }

    var notifications by rememberSaveable {
        mutableStateOf(true)
    }

    var securityAlerts by rememberSaveable {
        mutableStateOf(true)
    }

    val transactions = remember {
        mutableStateListOf(
            Transaction("Walmart", -34598L),
            Transaction("Direct Deposit", 150000L),
            Transaction("Exxon", -3546L)
        )
    }

    val colors = if (darkMode) {
        darkColorScheme(
            primary = Color(0xFF90CAF9)
        )
    } else {
        lightColorScheme(
            primary = Navy,
            secondary = BankBlue,
            background = LightBackground
        )
    }

    MaterialTheme(colorScheme = colors) {

        if (!loggedIn) {

            LoginScreen(
                onLogin = {
                    loggedIn = true
                    currentScreen = "Home"
                }
            )

        } else {

            Scaffold(

                containerColor =
                    MaterialTheme.colorScheme.background,

                topBar = {
                    BankHeader()
                },

                bottomBar = {

                    NavigationBar {

                        val tabs = listOf(
                            "Home" to "⌂",
                            "Accounts" to "▥",
                            "Settings" to "⚙",
                            "Profile" to "●"
                        )

                        tabs.forEach { (tab, symbol) ->

                            NavigationBarItem(
                                selected = currentScreen == tab,

                                onClick = {
                                    currentScreen = tab
                                },

                                icon = {
                                    Text(
                                        text = symbol,
                                        fontSize = 24.sp
                                    )
                                },

                                label = {
                                    Text(tab)
                                }
                            )
                        }
                    }
                }

            ) { padding ->

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {

                    when (currentScreen) {

                        "Home" -> HomeScreen(
                            balance = checkingBalance,
                            transactions = transactions,
                            onNavigate = {
                                currentScreen = it
                            }
                        )

                        "Accounts" -> AccountsScreen(
                            checkingBalance = checkingBalance,
                            savingsBalance = savingsBalance,
                            onNavigate = {
                                currentScreen = it
                            }
                        )

                        "Transfers" -> MoneyScreen(
                            title = "Transfer Money",
                            buttonText = "Transfer",
                            isTransfer = true,

                            onSubmit = { amount, reverse ->

                                if (!reverse) {

                                    if (amount > checkingBalance) {
                                        "Insufficient checking balance."
                                    } else {

                                        checkingBalance -= amount
                                        savingsBalance += amount

                                        transactions.add(
                                            0,
                                            Transaction(
                                                "Transfer to Savings",
                                                -amount
                                            )
                                        )

                                        null
                                    }

                                } else {

                                    if (amount > savingsBalance) {
                                        "Insufficient savings balance."
                                    } else {

                                        savingsBalance -= amount
                                        checkingBalance += amount

                                        transactions.add(
                                            0,
                                            Transaction(
                                                "Transfer from Savings",
                                                amount
                                            )
                                        )

                                        null
                                    }
                                }
                            },

                            onBack = {
                                currentScreen = "Home"
                            }
                        )

                        "Deposits" -> MoneyScreen(
                            title = "Deposit Money",
                            buttonText = "Deposit",

                            onSubmit = { amount, _ ->

                                checkingBalance += amount

                                transactions.add(
                                    0,
                                    Transaction(
                                        "Mobile Deposit",
                                        amount
                                    )
                                )

                                null
                            },

                            onBack = {
                                currentScreen = "Home"
                            }
                        )

                        "Pay Bills" -> MoneyScreen(
                            title = "Pay Bills",
                            buttonText = "Submit Payment",
                            isBillPayment = true,

                            onSubmit = { amount, _ ->

                                if (amount > checkingBalance) {

                                    "Insufficient funds."

                                } else {

                                    checkingBalance -= amount

                                    transactions.add(
                                        0,
                                        Transaction(
                                            "Bill Payment",
                                            -amount
                                        )
                                    )

                                    null
                                }
                            },

                            onBack = {
                                currentScreen = "Home"
                            }
                        )

                        "Transactions" -> {

                            SectionTitle("Transaction History")

                            Text(
                                "Checking account ••••0098"
                            )

                            Spacer(
                                modifier = Modifier.height(16.dp)
                            )

                            transactions.forEach { transaction ->
                                TransactionRow(transaction)
                            }

                            Spacer(
                                modifier = Modifier.height(16.dp)
                            )

                            OutlinedButton(
                                onClick = {
                                    currentScreen = "Home"
                                }
                            ) {
                                Text("Back to Home")
                            }
                        }

                        "Settings" -> SettingsScreen(
                            notifications = notifications,
                            securityAlerts = securityAlerts,
                            darkMode = darkMode,

                            onNotificationsChanged = {
                                notifications = it
                            },

                            onSecurityChanged = {
                                securityAlerts = it
                            },

                            onDarkModeChanged = {
                                darkMode = it
                            }
                        )

                        "Profile" -> ProfileScreen(
                            onLogout = {
                                loggedIn = false
                                currentScreen = "Home"
                            }
                        )
                    }
                }
            }
        }
    }
}

// Login screen

@Composable
fun LoginScreen(onLogin: () -> Unit) {

    var username by remember {
        mutableStateOf("")
    }

    var password by remember {
        mutableStateOf("")
    }

    var errorMessage by remember {
        mutableStateOf("")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                MaterialTheme.colorScheme.background
            )
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),

        horizontalAlignment = Alignment.CenterHorizontally,

        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "▥",
            fontSize = 65.sp,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "MyBank",
            fontSize = 38.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Text("Mobile Banking")

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = username,
            onValueChange = {
                username = it
            },
            label = {
                Text("Username")
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
            },
            label = {
                Text("Password")
            },
            singleLine = true,
            visualTransformation =
                PasswordVisualTransformation(),

            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password
            ),

            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (errorMessage.isNotEmpty()) {

            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error
            )
        }

        Button(
            onClick = {

                if (
                    username == "demo" &&
                    password == "bank123"
                ) {

                    onLogin()

                } else {

                    errorMessage =
                        "Invalid username or password."
                }
            },

            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Log In")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Demo Account",
            fontWeight = FontWeight.Bold
        )

        Text("Username: demo")
        Text("Password: bank123")

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Educational prototype only.",
            fontSize = 12.sp
        )
    }
}

// Application header

@Composable
fun BankHeader() {

    Surface(
        color = Navy,
        modifier = Modifier.fillMaxWidth()
    ) {

        Row(
            modifier = Modifier
                .statusBarsPadding()
                .padding(18.dp),

            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = "▥  MyBank",
                color = Color.White,
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "Mobile Banking",
                color = Color.White,
                fontSize = 12.sp
            )
        }
    }
}

// Home screen

@Composable
fun HomeScreen(
    balance: Long,
    transactions: List<Transaction>,
    onNavigate: (String) -> Unit
) {

    Text(
        text = "Hello, Felix",
        fontSize = 25.sp,
        fontWeight = FontWeight.Bold
    )

    Text("Welcome back to MyBank")

    Spacer(modifier = Modifier.height(20.dp))

    BalanceCard(
        accountName = "Checking Account",
        balance = balance,
        accountNumber = "0098"
    )

    Spacer(modifier = Modifier.height(24.dp))

    SectionTitle("Quick Actions")

    QuickActions(onNavigate)

    Spacer(modifier = Modifier.height(24.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),

        verticalAlignment = Alignment.CenterVertically
    ) {

        SectionTitle("Recent Transactions")

        Spacer(modifier = Modifier.weight(1f))

        TextButton(
            onClick = {
                onNavigate("Transactions")
            }
        ) {
            Text("View All")
        }
    }

    transactions.take(4).forEach { transaction ->
        TransactionRow(transaction)
    }
}

// Account balance card

@Composable
fun BalanceCard(
    accountName: String,
    balance: Long,
    accountNumber: String
) {

    Card(
        modifier = Modifier.fillMaxWidth(),

        shape = RoundedCornerShape(18.dp),

        colors = CardDefaults.cardColors(
            containerColor = Navy
        )
    ) {

        Column(
            modifier = Modifier.padding(24.dp)
        ) {

            Text(
                text = accountName,
                color = Color.White,
                fontSize = 18.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Available Balance",
                color = Color(0xFFCFD8E5),
                fontSize = 14.sp
            )

            Text(
                text = money(balance),
                color = Color.White,
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Account: ••••$accountNumber",
                color = Color.White
            )
        }
    }
}

// Quick action buttons

@Composable
fun QuickActions(
    onNavigate: (String) -> Unit
) {

    Column {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement =
                Arrangement.spacedBy(12.dp)
        ) {

            ActionButton(
                title = "Transfers",
                symbol = "⇄",
                modifier = Modifier.weight(1f),
                onClick = {
                    onNavigate("Transfers")
                }
            )

            ActionButton(
                title = "Deposits",
                symbol = "↓",
                modifier = Modifier.weight(1f),
                onClick = {
                    onNavigate("Deposits")
                }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement =
                Arrangement.spacedBy(12.dp)
        ) {

            ActionButton(
                title = "Transactions",
                symbol = "≡",
                modifier = Modifier.weight(1f),
                onClick = {
                    onNavigate("Transactions")
                }
            )

            ActionButton(
                title = "Pay Bills",
                symbol = "$",
                modifier = Modifier.weight(1f),
                onClick = {
                    onNavigate("Pay Bills")
                }
            )
        }
    }
}

@Composable
fun ActionButton(
    title: String,
    symbol: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {

    OutlinedCard(
        onClick = onClick,
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(12.dp)
    ) {

        Column(
            modifier = Modifier.fillMaxSize(),

            horizontalAlignment =
                Alignment.CenterHorizontally,

            verticalArrangement =
                Arrangement.Center
        ) {

            Text(
                text = symbol,
                fontSize = 28.sp,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = title,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// Accounts screen

@Composable
fun AccountsScreen(
    checkingBalance: Long,
    savingsBalance: Long,
    onNavigate: (String) -> Unit
) {

    SectionTitle("My Accounts")

    Text("Felix Danchenko")

    Spacer(modifier = Modifier.height(16.dp))

    BalanceCard(
        accountName = "Checking Account",
        balance = checkingBalance,
        accountNumber = "0098"
    )

    Spacer(modifier = Modifier.height(16.dp))

    BalanceCard(
        accountName = "Savings Account",
        balance = savingsBalance,
        accountNumber = "5678"
    )

    Spacer(modifier = Modifier.height(20.dp))

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text("Total Account Balance")

            Text(
                text = money(
                    checkingBalance + savingsBalance
                ),
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }

    Spacer(modifier = Modifier.height(24.dp))

    SectionTitle("Quick Actions")

    QuickActions(onNavigate)
}

// Transfers, deposits and bill payments

@Composable
fun MoneyScreen(
    title: String,
    buttonText: String,
    isTransfer: Boolean = false,
    isBillPayment: Boolean = false,
    onSubmit: (Long, Boolean) -> String?,
    onBack: () -> Unit
) {

    var amount by rememberSaveable {
        mutableStateOf("")
    }

    var payee by rememberSaveable {
        mutableStateOf("")
    }

    var reverseTransfer by rememberSaveable {
        mutableStateOf(false)
    }

    var message by rememberSaveable {
        mutableStateOf("")
    }

    var successful by rememberSaveable {
        mutableStateOf(false)
    }

    SectionTitle(title)

    Spacer(modifier = Modifier.height(16.dp))

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            if (isTransfer) {

                Text(
                    text = "Transfer From",
                    fontWeight = FontWeight.Bold
                )

                Text(
                    if (reverseTransfer) {
                        "Savings ••••5678"
                    } else {
                        "Checking ••••0098"
                    }
                )

                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                Text(
                    text = "Transfer To",
                    fontWeight = FontWeight.Bold
                )

                Text(
                    if (reverseTransfer) {
                        "Checking ••••0098"
                    } else {
                        "Savings ••••5678"
                    }
                )

                TextButton(
                    onClick = {
                        reverseTransfer = !reverseTransfer
                    }
                ) {
                    Text("Switch Accounts")
                }

            } else {

                Text(
                    text = "Checking Account ••••0098",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(20.dp))

    if (isBillPayment) {

        OutlinedTextField(
            value = payee,
            onValueChange = {
                payee = it
            },
            label = {
                Text("Company or Payee")
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))
    }

    OutlinedTextField(
        value = amount,

        onValueChange = {
            amount = it
        },

        label = {
            Text("Amount")
        },

        prefix = {
            Text("$")
        },

        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal
        ),

        singleLine = true,

        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(20.dp))

    Button(
        onClick = {

            val decimalAmount =
                amount.toBigDecimalOrNull()

            val enteredAmount = try {

                decimalAmount
                    ?.movePointRight(2)
                    ?.longValueExact()

            } catch (_: ArithmeticException) {
                null
            }

            successful = false

            if (
                enteredAmount == null ||
                enteredAmount <= 0 ||
                enteredAmount > 100000000L
            ) {

                message =
                    "Please enter a valid dollar amount."

            } else if (
                isBillPayment && payee.isBlank()
            ) {

                message = "Please enter a payee."

            } else {

                val error = onSubmit(
                    enteredAmount,
                    reverseTransfer
                )

                if (error == null) {

                    successful = true
                    message = "Transaction successful!"
                    amount = ""
                    payee = ""

                } else {

                    message = error
                }
            }
        },

        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
    ) {
        Text(buttonText)
    }

    Spacer(modifier = Modifier.height(16.dp))

    if (message.isNotEmpty()) {

        Text(
            text = message,

            color = if (successful) {
                DepositGreen
            } else {
                MaterialTheme.colorScheme.error
            },

            fontWeight = FontWeight.Bold
        )
    }

    Spacer(modifier = Modifier.height(20.dp))

    OutlinedButton(
        onClick = onBack,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Back to Home")
    }

    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = "This is a simulated transaction. " +
                "No real money will be moved.",
        fontSize = 12.sp
    )
}

// Transaction history

@Composable
fun TransactionRow(transaction: Transaction) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),

        shape = RoundedCornerShape(10.dp)
    ) {

        Row(
            modifier = Modifier.padding(16.dp),

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Text(
                text = if (transaction.amount >= 0) {
                    "↓"
                } else {
                    "↑"
                },

                fontSize = 24.sp,

                color = if (transaction.amount >= 0) {
                    DepositGreen
                } else {
                    MaterialTheme.colorScheme.error
                }
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = transaction.name,
                modifier = Modifier.weight(1f),
                fontWeight = FontWeight.Medium
            )

            Text(
                text = if (transaction.amount >= 0) {
                    "+${money(transaction.amount)}"
                } else {
                    money(transaction.amount)
                },

                color = if (transaction.amount >= 0) {
                    DepositGreen
                } else {
                    MaterialTheme.colorScheme.onSurface
                },

                fontWeight = FontWeight.Bold
            )
        }
    }
}

// Settings screen

@Composable
fun SettingsScreen(
    notifications: Boolean,
    securityAlerts: Boolean,
    darkMode: Boolean,
    onNotificationsChanged: (Boolean) -> Unit,
    onSecurityChanged: (Boolean) -> Unit,
    onDarkModeChanged: (Boolean) -> Unit
) {

    var selectedOption by remember {
        mutableStateOf("")
    }

    SectionTitle("Settings")

    Spacer(modifier = Modifier.height(16.dp))

    SectionTitle("Account Preferences")

    SettingSwitch(
        title = "Notifications",
        checked = notifications,
        onCheckedChange = onNotificationsChanged
    )

    SettingSwitch(
        title = "Security Alerts",
        checked = securityAlerts,
        onCheckedChange = onSecurityChanged
    )

    SettingSwitch(
        title = "Dark Mode",
        checked = darkMode,
        onCheckedChange = onDarkModeChanged
    )

    Spacer(modifier = Modifier.height(24.dp))

    SectionTitle("Additional Settings")

    MenuButton("Account Preferences") {
        selectedOption =
            "Your account preferences are managed " +
                    "locally in this prototype."
    }

    MenuButton("Security") {
        selectedOption =
            "This prototype uses demonstration login " +
                    "credentials and masked account numbers."
    }

    MenuButton("Help & Support") {
        selectedOption =
            "MyBank is a student project created " +
                    "for COP 4655."
    }

    MenuButton("About") {
        selectedOption =
            "MyBank Version 1.0\n" +
                    "Android Mobile Banking Prototype"
    }

    if (selectedOption.isNotEmpty()) {

        AlertDialog(
            onDismissRequest = {
                selectedOption = ""
            },

            title = {
                Text("MyBank")
            },

            text = {
                Text(selectedOption)
            },

            confirmButton = {

                TextButton(
                    onClick = {
                        selectedOption = ""
                    }
                ) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun SettingSwitch(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
    ) {

        Row(
            modifier = Modifier.padding(16.dp),

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Text(
                text = title,
                modifier = Modifier.weight(1f)
            )

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    }
}

// Profile screen

@Composable
fun ProfileScreen(onLogout: () -> Unit) {

    var selectedOption by remember {
        mutableStateOf("")
    }

    var confirmLogout by remember {
        mutableStateOf(false)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),

        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {

        Text(
            text = "●",
            fontSize = 70.sp,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "Felix Danchenko",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Text("Account ••••0098")

        Text("Member since 2023")
    }

    Spacer(modifier = Modifier.height(24.dp))

    SectionTitle("My Profile")

    MenuButton("Personal Information") {
        selectedOption =
            "Name: Felix Danchenko\n" +
                    "Account Type: Personal Banking"
    }

    MenuButton("Contact Information") {
        selectedOption =
            "Email: demo@example.com\n" +
                    "Phone: (555) 010-1234"
    }

    MenuButton("Linked Accounts") {
        selectedOption =
            "Checking Account ••••0098\n" +
                    "Savings Account ••••5678"
    }

    MenuButton("Documents") {
        selectedOption =
            "No documents are available " +
                    "in this demonstration."
    }

    Spacer(modifier = Modifier.height(24.dp))

    Button(
        onClick = {
            confirmLogout = true
        },

        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFB3261E)
        ),

        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Log Out")
    }

    if (selectedOption.isNotEmpty()) {

        AlertDialog(
            onDismissRequest = {
                selectedOption = ""
            },

            title = {
                Text("Profile Information")
            },

            text = {
                Text(selectedOption)
            },

            confirmButton = {

                TextButton(
                    onClick = {
                        selectedOption = ""
                    }
                ) {
                    Text("Close")
                }
            }
        )
    }

    if (confirmLogout) {

        AlertDialog(
            onDismissRequest = {
                confirmLogout = false
            },

            title = {
                Text("Log Out")
            },

            text = {
                Text("Are you sure you want to log out?")
            },

            confirmButton = {

                TextButton(
                    onClick = {
                        confirmLogout = false
                        onLogout()
                    }
                ) {
                    Text("Log Out")
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        confirmLogout = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

// Reusable menu button

@Composable
fun MenuButton(
    title: String,
    onClick: () -> Unit
) {

    OutlinedCard(
        onClick = onClick,

        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
    ) {

        Row(
            modifier = Modifier.padding(18.dp),

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Text(
                text = title,
                modifier = Modifier.weight(1f)
            )

            Text(
                text = "›",
                fontSize = 24.sp
            )
        }
    }
}

// Reusable section title

@Composable
fun SectionTitle(title: String) {

    Text(
        text = title,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}
