package com.example.nfccardrw

import android.app.PendingIntent
import android.content.Intent
import android.nfc.NfcAdapter
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.example.nfccardrw.ui.theme.NfccardreaderTheme

class MainActivity : ComponentActivity() {
    private lateinit var nfcManager: NfcManager
    private var nfcAdapter: NfcAdapter? = null
    private lateinit var pendingIntent: PendingIntent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        nfcManager = NfcManager(this)
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_MUTABLE
        )

        setContent {
            NfccardreaderTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NfcCardReaderWriterScreen(
                        nfcEnabled = nfcManager.isNfcEnabled(),
                        onWriteRequest = { content ->
                            Toast.makeText(this, "Tap an NFC tag to write", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        nfcAdapter?.enableForegroundDispatch(this, pendingIntent, null, null)
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter?.disableForegroundDispatch(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action) {
            intent.getParcelableExtra<android.nfc.Tag>(NfcAdapter.EXTRA_TAG)?.let { tag ->
                lifecycleScope.launch {
                    val result = nfcManager.readNfcTag(tag)
                    Toast.makeText(this@MainActivity, "Read: $result", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}

@Composable
fun NfcCardReaderWriterScreen(
    modifier: Modifier = Modifier,
    nfcEnabled: Boolean,
    onWriteRequest: (String) -> Unit
) {
    var cardContent by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "NFC Card Reader/Writer",
            style = MaterialTheme.typography.headlineMedium
        )

        OutlinedTextField(
            value = cardContent,
            onValueChange = { cardContent = it },
            label = { Text("Card Content") },
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = { Toast.makeText(context, "Tap an NFC tag to read", Toast.LENGTH_SHORT).show() },
                modifier = Modifier.weight(1f),
                enabled = nfcEnabled
            ) {
                Text("Read NFC Card")
            }

            Button(
                onClick = { onWriteRequest(cardContent) },
                modifier = Modifier.weight(1f),
                enabled = nfcEnabled && cardContent.isNotBlank()
            ) {
                Text("Write to NFC Card")
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Card Content: $cardContent",
                modifier = Modifier.padding(16.dp)
            )
        }

        if (!nfcEnabled) {
            Text(
                text = "NFC is not enabled. Please enable NFC in your device settings.",
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}
