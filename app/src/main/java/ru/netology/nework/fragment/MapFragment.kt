package ru.netology.nework.fragment

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.fragment.findNavController
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.layers.ObjectEvent
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.InputListener
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.user_location.UserLocationLayer
import dagger.hilt.android.AndroidEntryPoint
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.user_location.UserLocationObjectListener
import com.yandex.mapkit.user_location.UserLocationView
import com.yandex.runtime.ui_view.ViewProvider
import ru.netology.nework.R
import ru.netology.nework.databinding.FragmentMapBinding
import ru.netology.nework.databinding.LocationBinding
import ru.netology.nework.viewmodel.PostViewModel
import kotlin.getValue

@AndroidEntryPoint
class MapFragment : Fragment() {


    private val viewModel: PostViewModel by activityViewModels()

    private var mapView: MapView? = null
    private lateinit var userLocation: UserLocationLayer

    private val listener = object : InputListener {
        override fun onMapTap(map: Map, point: Point) = Unit

        override fun onMapLongTap(
            map: Map,
            point: Point
        ) {
            viewModel.setLocation(point.latitude, point.longitude)
        }
    }

    private val locationObjectListener = object : UserLocationObjectListener {
        override fun onObjectAdded(userLocationView: UserLocationView) = Unit

        override fun onObjectRemoved(userLocationView: UserLocationView) = Unit

        override fun onObjectUpdated(
            userLocationView: UserLocationView,
            objectEvent: ObjectEvent
        ) {
            userLocation.cameraPosition()?.target?.let {
                mapView?.mapWindow?.map?.move(CameraPosition(it, 10F, 0F, 0F))
            }
            userLocation.setObjectListener(null)
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                MapKitFactory.getInstance().resetLocationManagerToDefault()
                userLocation.cameraPosition()?.target?.also {
                    val map = mapView?.mapWindow?.map ?: return@registerForActivityResult
                    val cameraPosition = map.cameraPosition
                    map.move(
                        CameraPosition(
                            it,
                            cameraPosition.zoom,
                            cameraPosition.azimuth,
                            cameraPosition.tilt,
                        )
                    )
                }
            } else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.permission_required),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = FragmentMapBinding.inflate(inflater, container, false)

        mapView = binding.map.apply {
            userLocation = MapKitFactory.getInstance().createUserLocationLayer(mapWindow)
            userLocation.isVisible = true
            userLocation.isHeadingModeActive = false

            mapWindow.map.addInputListener(listener)
            val collection = mapWindow.map.mapObjects.addCollection()

            viewModel.edited.observe(viewLifecycleOwner) { post ->
                collection.clear()
                val placeBinding = LocationBinding.inflate(layoutInflater)
                if (post.coords != null) {
                    val point = Point(post.coords.lat, post.coords.long)

                    collection.addPlacemark(
                        point,
                        ViewProvider(placeBinding.root)
                    )

                    val cameraPosition = mapWindow.map.cameraPosition
                    mapWindow.map.move(
                        CameraPosition(
                            Point(post.coords.lat, post.coords.long),
                            10F,
                            cameraPosition.azimuth,
                            cameraPosition.tilt,
                        )
                    )
                } else {
                    userLocation.setObjectListener(locationObjectListener)
                }
            }
        }

        with(binding) {
            zoomIn.setOnClickListener {
                map.mapWindow.map.move(
                    CameraPosition(
                        map.mapWindow.map.cameraPosition.target,
                        map.mapWindow.map.cameraPosition.zoom + 1, 0.0f, 0.0f
                    ),
                    Animation(Animation.Type.SMOOTH, 1F),
                    null
                )
            }

            zoomOut.setOnClickListener {
                map.mapWindow.map.move(
                    CameraPosition(
                        map.mapWindow.map.cameraPosition.target,
                        map.mapWindow.map.cameraPosition.zoom - 1, 0.0f, 0.0f
                    ),
                    Animation(Animation.Type.SMOOTH, 1F),
                    null
                )
            }

            removeLocation.setOnClickListener {
                viewModel.removeLocation()
            }

            myLocation.setOnClickListener {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }

        requireActivity().addMenuProvider(
            object : MenuProvider {
                override fun onCreateMenu(
                    menu: Menu,
                    menuInflater: MenuInflater
                ) {
                    menuInflater.inflate(R.menu.menu_top_app_bar, menu)
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
                    when (menuItem.itemId) {
                        R.id.save -> {
                            findNavController().navigateUp()
                            true
                        }

                        R.id.clear -> {
                            viewModel.removeLocation()
                            true
                        }

                        else -> false
                    }
            }, viewLifecycleOwner
        )


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = FragmentMapBinding.bind(view)

        val mapView = binding.map
        subscribeToLifecycle(mapView)
    }

    private fun subscribeToLifecycle(mapView: MapView) {
        viewLifecycleOwner.lifecycle.addObserver(
            object : LifecycleEventObserver {
                override fun onStateChanged(
                    source: LifecycleOwner,
                    event: Lifecycle.Event
                ) {
                    when (event) {
                        Lifecycle.Event.ON_START -> {
                            MapKitFactory.getInstance().onStart()
                            mapView.onStart()
                        }

                        Lifecycle.Event.ON_STOP -> {
                            mapView.onStop()
                            MapKitFactory.getInstance().onStop()
                        }

                        Lifecycle.Event.ON_DESTROY -> source.lifecycle.removeObserver(this)

                        else -> Unit
                    }
                }

            }
        )
    }
}