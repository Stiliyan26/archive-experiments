import express from 'express'
import morganLogger from 'morgan'
import path from 'path'
import open from 'open';
import multer from 'multer';
import bodyParser from 'body-parser'

const HOST = '127.0.0.1';
const PORT_DEV = 2337;
const PORT_DIST = 5000;

let args = process.argv.slice(2);
let env = args[args.indexOf('--env') + 1] || 'dev';
let PORT = env == 'dev' ? PORT_DEV : PORT_DIST;

var app = express();
process.env.NODE_ENV = env;

console.log('========================================================================================')
console.log(`---------- 👉 NODE_ENV selected: ${process.env.NODE_ENV}`)
console.log(`---------- 👉 BABEL_ENV selected: ${process.env.BABEL_ENV || process.env.NODE_ENV}`)

app.use(bodyParser())

if(env == 'dist'){
	app.use('/', express.static(path.resolve(__dirname, '../dist/')))
	app.use(express.static(path.resolve(__dirname, '../dist/assets/')))
}
else if(env == 'dev'){
	// import { hotMiddleware } from './middleware/hot';
	var hotMiddleware = require('./middleware/hot').hotMiddleware;
	// Hot reloading + bundler
	app.use(hotMiddleware);
	// pointing to the root folder "src"
	// this is need in order to properly locate the favicon
	app.use('/', express.static(__dirname))
}

// Logging middleware
if(process.argv.indexOf('--logger') != -1){
	app.use(morganLogger('[:date[clf]] :remote-addr :method :url :status '))
}

app.get('*', function(req, res){
	res.sendFile(path.join(__dirname, './index.html'));
})


// configuring Multer to use files directory for storing files
// this is important because later we'll need to access file path
const storage = multer.diskStorage({
  destination: function (request, file, callback) {
        callback(null, './uploads/');
    },
  filename(req, file, cb) {
  	// including the current date in the uploaded file name
    cb(null, `${new Date().toLocaleDateString()}-${file.originalname}`);
  },
});

const upload = multer({ storage });

// express route where we receive files from the client
// passing multer middleware
app.post('/files',
			upload.single('file'),
			(req, res) => {
				const file = req.body.file; // file passed from client
				const meta = req.body; // all other values passed from the client, like name, etc..
			}
);

app.post('/contract', (req, res) => {
	let reqBody = req.body;
	// console.log('======================================================== /Contract req.body')
	// console.log(reqBody)
	let contract = reqBody.contract;
	res.status(200)
	res.send({message: "Successfully created a contract!"})
})
app.post('/invoice', (req, res) => {
	let reqBody = req.body;
	let contract = reqBody.contract;
	res.status(200)
	res.send({message: "Successfully created an invoice!"})
})
app.post('/payment', (req, res) => {
	res.status(200)
	res.send({message: "Successfully create a payment!"})
})

app.post('/salt', (req, res) => {
	let username = req.body.username

	res.setHeader('Content-Type', 'application/json')
	if(username === "admin"){
		res.send(JSON.stringify({
			salt: "76g2135717v23c172536"
		}))
	}
	else{
		res.status(404)
		res.send({error: "Incorrect username or password"});
	}
})

// app.post('/authenticate', (req, res) => {
// 		res.setHeader('Content-Type', 'application/json');
// 		res.send(JSON.stringify({
// 			ok: true,
// 			csrf_token: "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWV9.TJVA95OrM7E2cBab30RMHrHDcEfxjoYZgeFONFh7HgQ"
// 		}))
// })

// Start the server
console.log(`---------- 🏅 Express server is listening on: http://${HOST}:${PORT} `)
console.log('========================================================================================\r\n')

app.listen(PORT, function (error) {
		if(error) {
				console.log(error);
		}
		else{
			open(`http://${HOST}:${PORT}/#/home`)
		}
});