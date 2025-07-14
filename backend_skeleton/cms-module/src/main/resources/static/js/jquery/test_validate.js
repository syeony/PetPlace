function validator()
{
	$("#aform").validate({
		onkeyup : false
		, onfocusout : false
	    , rules:
	    {
	    	jumin1:		{ required : true, maxlength : 6 						}
			, jumin2: 	{ required : true, minlength : 1, maxlength : 7			}
	    	, c_name: 	{ required : true, maxlength : 12		 				}
			, c_phone: 	{ required : true, checkPhone : true 					}
			, c_money: 	{ required : true, checkNum : true 						}
			, c_email:	{ required : true, email : true							}
	    },
	    messages: 
	    {
	    	jumin1:
	    	{
		        required : "주민번호은 필수 입력입니다."
		        , maxlength: "이름은 {0}자 이하로 넣어주십시오."
	    	}
	    	, jumin2:
	    	{
	    		required : "주민번호은 필수 입력입니다."
    			, minlength: "이름은 {0}자 이상으로 넣어주십시오."
		        , maxlength: "이름은 {0}자 이하로 넣어주십시오."
	    	}
	    	, c_name:
	    	{
		        required : "이름은 필수 입력입니다."
		        , maxlength: "이름은 {0}자 이하로 넣어주십시오."
	    	}
	    	, c_phone:
	    	{
		        required : "연락처는 필수 입력입니다."
		        , checkPhone: "연락처 양식에 맞춰주세요"
	    	}
	    	, c_money:
	    	{
		        required : "재판매금액은 필수 입력입니다."
		        , checkNum: "재판매금액은 숫자만 가능합니다"
	    	}
	    	, c_email:
	    	{
	    		required : "이메일은 필수 입력입니다."
		        , email : "이메일 양식을 맞춰주세요."
	    	}
	    }
	    , showErrors: function(error, element) 
	    {	
	    	if(element.length > 0) 
	    	{ 
	    		alert(element[0].message);
	    		element[0].element.focus();
	    	}
	    }
	    , submitHandler : function(form)
	    {
	    	// 주민번호 비교 검색
	    	if(form.flag.value == 'jumin') { f_JuminCompare(); }
	    	// 양도 신청
	    	if(form.flag.value == 'insert') { f_Submit(); }
	    }
	});
}