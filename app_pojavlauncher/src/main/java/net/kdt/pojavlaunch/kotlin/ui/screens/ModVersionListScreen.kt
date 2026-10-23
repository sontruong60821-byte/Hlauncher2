package net.kdt.pojavlaunch.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import android.widget.ExpandableListAdapter
import android.widget.ExpandableListView

@Composable
fun ModVersionListScreen(
    title: String,
    adapter: ExpandableListAdapter?,
    isLoading: Boolean,
    showRetry: Boolean,
    retryMessage: String,
    onRetry: () -> Unit,
    onChildClick: (selected: Any?) -> Unit
) {
    Surface(color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            HorizontalDivider()

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }

            AndroidView(
                factory = { ctx ->
                    ExpandableListView(ctx).apply {
                        setOnChildClickListener { _, _, groupPosition: Int, childPosition: Int, _: Long ->
                            onChildClick(adapter?.getChild(groupPosition, childPosition))
                            true
                        }
                    }
                },
                update = { listView ->
                    listView.setAdapter(adapter)
                    listView.isEnabled = !isLoading
                    listView.setOnChildClickListener { _, _, groupPosition: Int, childPosition: Int, _: Long ->
                        onChildClick(adapter?.getChild(groupPosition, childPosition))
                        true
                    }
                },
                modifier = Modifier.weight(1f).fillMaxWidth()
            )

            if (showRetry) {
                Text(
                    text = retryMessage,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.SemiBold
                )
                TextButton(onClick = onRetry) {
                    Text("Retry")
                }
            }
        }
    }
}
