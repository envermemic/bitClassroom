/**
 * Created by NN on 14.9.2015.
 */


function checkEmail() {
    var email = document.getElementById("inputEmail").value;
    var filter = /^([\w-]+(?:\.[\w-]+)*)@((?:[\w-]+\.)*\w[\w-]{0,66})\.([a-z]{2,6}(?:\.[a-z]{2})?)$/i;


    if (!filter.test(email)) {
        document.getElementById("emailError").innerHTML = "Invalid E-mail.";
    } else if(email.search("@bitcamp.ba") == -1){
        document.getElementById("emailError").innerHTML = "Must contains @bitcamp.ba";
    }else if ( filter.test(email) && email.search("@bitcamp.ba")){
        document.getElementById("emailError").innerHTML = "";
    }
}


function checkNameInput() {


    var firstName = document.getElementById("inputName").value;
    var filter = /^[a-zA-Z]+$/;


    if (firstName.length == 0) {
        document.getElementById("nameError").innerHTML = "This field is required";
    } else {


        if (!filter.test(firstName)) {
            document.getElementById("nameError").innerHTML = "Only letters allowed.";
        } else  if (firstName.length < 4){
            document.getElementById("nameError").innerHTML = "Nick name is short";
        }else{

            document.getElementById("nameError").innerHTML = "";
        }
    }
}



function checkPassword() {
        var password = document.getElementById("inputPassword1").value;


         if (password.length == 0) {
                 document.getElementById("passError").innerHTML = "This field is required";
             } else {




                 if (password.length < 6) {
                         document.getElementById("passError").innerHTML = "Password must have at least 6 characters.";
                     } else if (password.search(/\d/) == -1) {
                         document.getElementById("passError").innerHTML = "Password must have at least one number.";
                     } else if (password.search(/[a-zA-Z]/)) {
                         document.getElementById("passError").innerHTML = "Password must have at least one letter.";
                     } else if (password.length > 6 && !password.search(/[a-zA-Z]/) && password.search(/\d/) != -1) {
                         document.getElementById("passError").innerHTML = "";
                     }
             }
}


function checkPasswords() {
          var password = document.getElementById("inputPassword1").value;
          var repassword = document.getElementById("inputPassword").value;

              if (password != repassword) {
                 document.getElementById("repassError").innerHTML = "Password must be same.";
             } else {
                 document.getElementById("repassError").innerHTML = "";
              }
}

function checkPhone(){


        var phone = document.getElementById("home-phone").value;
        var phoneno = /^\+?([0-9]{3})\)?[-. ]?([0-9]{2})[-. ]?([0-9]{3})[-. ]?([0-9]{3})$/;

        if (!(phone.length == 15)){
            document.getElementById("phoneCheck").innerHTML = "Enter correct form of phone number[ +387-XX-XXX-XXX ]";
        }else if (!(phone.match(phoneno))){
            document.getElementById("phoneCheck").innerHTML = "Enter only numbers";

        }else if(phone.length >=10 && phone.length<=15 && phone.match(phoneno)){

            document.getElementById("phoneCheck").innerHTML = "";
        }

}

function checkMobilePhone(){


    var phone = document.getElementById("mobile-phone").value;
    var phoneno = /^\+?([0-9]{3})\)?[-. ]?([0-9]{2})[-. ]?([0-9]{3})[-. ]?([0-9]{3})$/;

    if (!(phone.length == 15)){
        document.getElementById("mobilePhoneCheck").innerHTML = "Enter correct form of phone number[ +387-XX-XXX-XXX ]";
    }else if (!(phone.match(phoneno))){
        document.getElementById("mobilePhoneCheck").innerHTML = "Enter only numbers";

    }else if(phone.length >=10 && phone.length<=15 && phone.match(phoneno)){

        document.getElementById("mobilePhoneCheck").innerHTML = "";
    }

}

function checkSkypeName(){

    var skypeName = document.getElementById("skype").value;
    var filter = /^[a-zA-Z]+$/;

    if ( skypeName.length < 3){

        document.getElementById("skypeCheck").innerHTML = "Too short";

    }else if (!filter.test(skypeName)){

        document.getElementById("skypeCheck").innerHTML =  "Skype name must have at least one letter.";
    }else if (skypeName >= 3 ||  filter.test(skypeName)){
        document.getElementById("skypeCheck").innerHTML =  "";
    }

}

function checkFacebook(){

    var facebook = document.getElementById("facebook").value;

    if ( facebook == null){

        facebook.val("http://www.facebook.com/");
    }else if ( facebook.length < 30){

        document.getElementById("facebookCheck").innerHTML = " Wrong URL, please enter valid URL";
    }else if ( facebook.length >= 30) {

        document.getElementById("facebookCheck").innerHTML = "";
    }



}



