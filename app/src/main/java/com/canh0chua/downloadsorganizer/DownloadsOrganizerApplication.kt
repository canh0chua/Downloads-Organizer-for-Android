package com.canh0chua.downloadsorganizer

import android.app.Application
import android.content.pm.PackageManager
import android.graphics.drawable.BitmapDrawable
import androidx.core.graphics.drawable.toBitmap
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.DataSource
import coil.decode.VideoFrameDecoder
import coil.fetch.DrawableResult
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import coil.request.Options
import java.io.File

class DownloadsOrganizerApplication : Application(), ImageLoaderFactory {
    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .components {
                add(VideoFrameDecoder.Factory())
                add(ApkIconFetcher.Factory(this@DownloadsOrganizerApplication))
            }
            .crossfade(true)
            .build()
    }

    class ApkIconFetcher(
        private val data: File,
        private val context: Application
    ) : Fetcher {
        override suspend fun fetch(): FetchResult? {
            val packageManager = context.packageManager
            val packageInfo = packageManager.getPackageArchiveInfo(data.absolutePath, 0) ?: return null
            val applicationInfo = packageInfo.applicationInfo
            applicationInfo.sourceDir = data.absolutePath
            applicationInfo.publicSourceDir = data.absolutePath
            val icon = applicationInfo.loadIcon(packageManager)
            return DrawableResult(
                drawable = icon,
                isSampled = false,
                dataSource = DataSource.DISK
            )
        }

        class Factory(private val context: Application) : Fetcher.Factory<File> {
            override fun create(data: File, options: Options, imageLoader: ImageLoader): Fetcher? {
                if (data.extension.lowercase() in listOf("apk", "xapk", "apkm", "xapkm", "eapk")) {
                    return ApkIconFetcher(data, context)
                }
                return null
            }
        }
    }
}
