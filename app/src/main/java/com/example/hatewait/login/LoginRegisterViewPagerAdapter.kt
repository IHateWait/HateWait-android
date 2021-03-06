package com.example.hatewait.login

import com.example.hatewait.lottie.LottieDialogFragment.Companion.fragment
import com.example.hatewait.lottie.LottieDialogFragment.Companion.newInstance
import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import com.example.hatewait.*
import com.example.hatewait.login.LoginInfo.storeInfo
import com.example.hatewait.model.MemberRegisterRequestData
import com.example.hatewait.model.MemberRegisterResponseData
import com.example.hatewait.register.MemberRegister
import com.example.hatewait.register.NameCheckDialogFragment
import com.example.hatewait.register.RegisterCheck
import com.example.hatewait.register.RegisterErrorDialogFragment
import com.example.hatewait.retrofit2.MyApi
import com.example.hatewait.retrofit2.RetrofitLogin
import com.example.hatewait.retrofit2.RetrofitRegister
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.activity_members_register.*
import kotlinx.android.synthetic.main.activity_register_tab_pager.*
import org.jetbrains.anko.startActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class LoginRegisterViewPagerActivity : AppCompatActivity(),
    RegisterErrorDialogFragment.RegisterMemberDialogListener,
    NameCheckDialogFragment.NameCheckListener,
    MemberRegister.CustomerInfoListener {

    private lateinit var newCustomerName: String
    private var newCustomerTurn = -1
    private val tabNameArray = arrayOf<String>("비회원", "회원")
    lateinit var mContext: Context

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_tab_pager)
//        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        mContext = this
        init()

    }

    private fun init() {
        view_pager.adapter =
            ScreenSlidePagerAdapter(this)
        TabLayoutMediator(kind_of_member_tab_layout, view_pager) { tab, position ->
            tab.text = tabNameArray[position]
        }.attach()
        //attach() :    Link the TabLayout and the ViewPager2 together.
        // Must be called after ViewPager2 has an adapter set

    }

    override fun onDialogPositiveClick(
        dialog: DialogFragment,
        customer_id: String,
        customer_people_num: Int
    ) {

        var memberRegisterData = MemberRegisterRequestData(customer_id, customer_people_num.toString(), true)
//        Log.d("retrofit2 회원 대기 등록 :: ", memberRegisterData.toString())
        Log.d("retrofit2 회원 대기 등록 :: ", memberRegisterData.toString())

        if (fragment == null || (!(fragment?.isAdded)!!)) {
            newInstance().show(supportFragmentManager, "")
        }
        MyApi.RetrofitAdapter.retrofit(this)!!.create(RetrofitRegister::class.java).requestMemberRegister(storeInfo.id, memberRegisterData)
            .enqueue(object : Callback<MemberRegisterResponseData> {
                override fun onFailure(
                    call: Call<MemberRegisterResponseData>,
                    t: Throwable
                ) {
                    Log.d("retrofit2 회원 대기 등록 :: ", "연결실패 $t")
                }

                override fun onResponse(
                    call: Call<MemberRegisterResponseData>,
                    response: Response<MemberRegisterResponseData>
                ) {
                    newInstance().dismiss()
                    var data: MemberRegisterResponseData? = response?.body() // 서버로부터 온 응답
                    Log.d(
                        "retrofit2 회원 대기 등록 ::",
                        response.code().toString() + data
                    )
                    when (response.code()) {
                        201 -> {

                            startActivity<RegisterCheck>(
                                "CUSTOMER_NAME" to data!!.name,
                                "CUSTOMER_TURN" to data!!.count
                            )
                            dialog.dismiss()
                        }
                        409 -> {
                            Toast.makeText(mContext, "이미 다른 가게에 등록된 아이디입니다.", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                }
            })
    }

    override fun onDialogNegativeClick(dialog: DialogFragment) {
        Toast.makeText(this, "아이디를 확인헤주세요!", Toast.LENGTH_SHORT).show()
        dialog.dismiss()
    }

    override fun applyText(memberId: String) {
        user_id_input_editText.setText(memberId)
    }

    override fun registerCustomer(memberRegister: MemberRegister) {
        newCustomerName = memberRegister.customerName!!
        newCustomerTurn = memberRegister.customerTurn!!
    }


}
