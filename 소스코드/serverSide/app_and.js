const express = require('express');
const app = express();
const ejs = require("ejs");

app.set("views",__dirname + "/views");
app.set("view engine","ejs");
app.engine("ejs", ejs.renderFile);

const mysql = require("mysql");

const conn_info = {  
    host : "localhost",
    port : 3306,
    user : "root",
    password : "1234",
    database : "qrGenerator"
};

const conn = mysql.createConnection(conn_info);

var registerQuery = 'insert into users (uid, password, name, phone) VALUES (?, ?, ?, ?)';
var duplicationQuery = "select count(*) from users where uid=?";
//로그인:아이디 있는지 확인, 비밀번호 일치확인, 토큰 등록
var pwCheckQuery = "select password from users where uid = ?";
var loginQuery = "insert into logtoken (uid, token) VALUES (?, ?)";
var mainQuery = "select uid, name, phone, message from users where uid = ?";
var messageQuery = "update users set message=? where uid = ?";
var logout = "drop"; //토큰삭제

let user ;

conn.connect((err)=>{
    if(!err){
        console.log("database connected successfully");
    }
    else{
        console.log("database connected failed : ", err);    
    }
});

var idCheck;
app.post('/idCheck', (req, res)=>{
    console.log("idCheck mode");
    var inputData;
    
    req.on('data', (data)=>{
        console.log("idCheck --data entry--");
        inputData = JSON.parse(data);
    });
    req.on('end', ()=>{
        console.log("idCheck --end entry--");
        conn.query(duplicationQuery, inputData.uid, (err, rows)=>{
            if(rows[0]['count(*)'] == 1){
                console.log(rows);
                console.log("중복된 아이디 입니다.");
                idCheck = "_xx_" + inputData.uid ; 
            }else{
                console.log("회원가입 가능");
                idCheck = inputData.uid;
            }
            console.log(idCheck);
        });
    });
    res.write(idCheck);
    res.end();
});

app.post('/register', (req, res)=>{
    console.log("verify duplication");
    var inputData ;
    var registerCHK ;
    var qinput;
    req.on('data', (data)=>{
        console.log("request data ----");
        inputData = JSON.parse(data);
        qinput = [inputData.uid, inputData.password, inputData.name, inputData.phone];
        console.log("register test log--------" , inputData.uid);
    });

    req.on('end', () => {
        conn.query(registerQuery, qinput, (err)=>{
            if(!err){
                console.log("register success");
                registerCHK = "OK";
            }
            else{
                console.log("register failed");
                // console.log(err);
                registerCHK = "NO";
            }
        });
    });
    res.write(registerCHK);
    res.end(); 
});

var ouputText ;
app.post('/login', (req, res) => {
    console.log("-------login mode-------");
    var inputData;
    var pwCheck = false;
    var idCheck = false;
    var loginOK ;
    var qinput;

    req.on('data', (data)=>{
        console.log("request data ----");
        inputData = JSON.parse(data);
        qinput = [inputData.uid, inputData.password];
        console.log("login test log" , inputData.uid);
    });
    req.on('end', () => {
        conn.query(duplicationQuery, inputData.uid, (err, rows)=>{
            if(rows[0]['count(*)'] == 1){
                console.log(rows);
                idCheck = true;
                conn.query(pwCheckQuery, inputData.uid, (err, rows)=>{
                    if(rows[0]['password'] == inputData.password){
                        console.log("패스워드 일치");
                        // var token = Math.floor(Math.random() * 9999999);
                        // consolel.log("token : " + token);
                        var token = 9999;//성공토큰
                        pwCheck = true;
                        conn.query(loginQuery, [qinput[0], token] ,(err,rows)=>{
                            loginOK = true;
                            console.log("로그인 성공");
                            ouputText="login_OK";
                        })
                    }else{
                        console.log("패스워드 불일치");
                        ouputText = "invalid_PW";
                    }
                });
            }
            else{
                console.log("존재하지 않는 ID");
                ouputText = "invalid_ID";
            }
        });
    });
    res.write(ouputText);
    res.end();
});

app.post('/mainpage', (req, res) => {
    
    var inputData;
    req.on('data', (data)=>{
        console.log("mainpage data on");
        inputData = JSON.parse(data).uid;
        conn.query(mainQuery, inputData, (err, rows)=>{
            user = rows;
            console.log(user);
        });
    });
    req.on('end',()=>{

    });
    
   res.json(user);
//    res.end();
});

app.post("/message", (req,res)=>{
    var inputData ;
    req.on('data', (data)=>{
        console.log("message on");
        inputData = JSON.parse(data);
        var qinput = [inputData.message, inputData.uid];
        conn.query(messageQuery, qinput, (err, rows)=>{
            if(!err)
                console.log("message Query 성공");
        });
    });
    req.on('end',()=>{

    });
});

app.get("/mypage",(req, res)=>{

    var id = req.query.id;
    console.log("get query---------" + id);
    var _name ;
    var _phone ;
    var _message;

    conn.query(mainQuery,id,(err, row)=>{
        var dbData = row[0];
        _name = dbData.name;
        _phone = dbData.phone;
        _message = dbData.message;

        var render_data = {
            uid : id,
            name : _name,
            phone : _phone,
            message : _message
        };

        console.log(_name);
        console.log(_phone);
        res.render("mypage.ejs", render_data);
        
    });

    
});


app.listen(3000, () => {
  console.log('Example app listening on port 3000!');
});

