# Proxy Server

A simple **proxy server** built with NestJS, which forwards requests from the frontend to other backend APIs, attaches `x-api-key`, and supports CORS.

---

## 1. Installation

1. Clone the project:

```bash
git clone <repo-url>
cd <project-folder>
```

2. Install dependencies:

```bash
npm install
```

---

## 2. Environment Configuration

Create a `.env` file in the root project with the following variables:

```env
# Proxy server port
PORT=

# API1
API1_KEY=
API1_CLIENT_URL=
API1_SERVER_URL=

# (You can add API2, API3 ... if needed)
```

**Explanation:**

- `PORT`: The port the proxy server will run on.
- `API{n}_KEY`: API key to forward requests to the backend.
- `API{n}_CLIENT_URL`: Frontend URLs allowed to call the proxy (CORS).
- `API{n}_SERVER_URL`: Backend URL where the proxy forwards requests.

---

## 3. Run the Proxy Server

```bash
npm run start:dev
```

After starting, the proxy server listens on `http://localhost:PORT`.

---

## 4. Usage

Frontend calls the proxy server like this:

```
<PROXY_SERVER_URL>/proxy/<API_NAME>/<PATH>
```

Example:

```
http://localhost:3333/proxy/api1/users
```

- The proxy automatically attaches `x-api-key` and forwards the request to `API1_SERVER_URL`.
- Supports all HTTP methods: GET, POST, PUT, DELETE, PATCH.
- **Remember to configure your frontend Axios with `withCredentials: true`**.

---

## 5. Notes

- Only frontend URLs listed in `API{n}_CLIENT_URL` are allowed to call the proxy.
- Requests to the proxy retain original headers, body, and method.
- Check console logs for debugging requests and responses.
