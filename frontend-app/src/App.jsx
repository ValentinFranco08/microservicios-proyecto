import { useEffect, useMemo, useState } from "react";
import {
  AlertCircle,
  CheckCircle2,
  ClipboardList,
  LogIn,
  LogOut,
  PackagePlus,
  RefreshCw,
  ShieldCheck,
  UserPlus,
  Users
} from "lucide-react";

const AUTH_API = import.meta.env.VITE_AUTH_API_URL || "http://localhost:18003/auth-service";
const USUARIO_API = import.meta.env.VITE_USUARIO_API_URL || "http://localhost:18001/usuario-service";
const PEDIDO_API = import.meta.env.VITE_PEDIDO_API_URL || "http://localhost:18002/pedido-service";

const initialBusinessUser = {
  email: "juan@example.com",
  nombre: "Juan",
  apellido: "Perez",
  telefono: "1123456789",
  direccion: "Calle Principal 123",
  ciudad: "Buenos Aires",
  pais: "Argentina"
};

const initialPedido = {
  usuarioId: "",
  numeroProducto: "PROD-001",
  nombreProducto: "Laptop Dell XPS",
  cantidad: 1,
  precioUnitario: 1200,
  descripcion: "Laptop de gama alta",
  direccionEnvio: "Calle Principal 123, Buenos Aires"
};

function App() {
  const [authForm, setAuthForm] = useState({ username: "juan", password: "123456" });
  const [token, setToken] = useState(() => localStorage.getItem("jwtToken") || "");
  const [authUser, setAuthUser] = useState(() => localStorage.getItem("authUser") || "");
  const [authMessage, setAuthMessage] = useState(null);
  const [businessUser, setBusinessUser] = useState(initialBusinessUser);
  const [pedidoForm, setPedidoForm] = useState(initialPedido);
  const [usuarios, setUsuarios] = useState([]);
  const [pedidos, setPedidos] = useState([]);
  const [selectedPedido, setSelectedPedido] = useState(null);
  const [statusMessage, setStatusMessage] = useState(null);
  const [loading, setLoading] = useState(false);

  const isAuthenticated = Boolean(token);

  const tokenPreview = useMemo(() => {
    if (!token) return "Sin token";
    return `${token.slice(0, 18)}...${token.slice(-12)}`;
  }, [token]);

  useEffect(() => {
    if (isAuthenticated) {
      refreshData(false);
    }
  }, [isAuthenticated]);

  async function request(url, options = {}) {
    const headers = {
      "Content-Type": "application/json",
      ...(options.headers || {})
    };

    const response = await fetch(url, {
      ...options,
      headers
    });

    const text = await response.text();
    const data = text ? parseJson(text) : null;

    if (!response.ok) {
      const detail = typeof data === "string" ? data : data?.error || text || response.statusText;
      throw new Error(detail);
    }

    return data;
  }

  function authHeaders() {
    return token ? { Authorization: `Bearer ${token}` } : {};
  }

  async function createAuthUser(event) {
    event.preventDefault();
    setAuthMessage(null);
    setLoading(true);
    try {
      await request(`${AUTH_API}/create-user`, {
        method: "POST",
        body: JSON.stringify(authForm)
      });
      setAuthMessage({ type: "success", text: "Usuario de autenticacion creado." });
    } catch (error) {
      setAuthMessage({ type: "error", text: error.message });
    } finally {
      setLoading(false);
    }
  }

  async function login(event) {
    event.preventDefault();
    setAuthMessage(null);
    setLoading(true);
    try {
      const data = await request(`${AUTH_API}/login`, {
        method: "POST",
        body: JSON.stringify(authForm)
      });
      setToken(data.token);
      setAuthUser(authForm.username);
      localStorage.setItem("jwtToken", data.token);
      localStorage.setItem("authUser", authForm.username);
      setAuthMessage({ type: "success", text: "Sesion iniciada. Token JWT guardado." });
    } catch (error) {
      setAuthMessage({ type: "error", text: error.message });
    } finally {
      setLoading(false);
    }
  }

  function logout() {
    setToken("");
    setAuthUser("");
    setPedidos([]);
    setSelectedPedido(null);
    localStorage.removeItem("jwtToken");
    localStorage.removeItem("authUser");
    setAuthMessage({ type: "success", text: "Sesion cerrada." });
  }

  async function refreshData(showMessage = true) {
    setLoading(true);
    try {
      const [usuariosData, pedidosData] = await Promise.all([
        request(`${USUARIO_API}/api/v1/usuarios`),
        request(`${PEDIDO_API}/api/v1/pedidos`, { headers: authHeaders() })
      ]);
      setUsuarios(Array.isArray(usuariosData) ? usuariosData : []);
      setPedidos(Array.isArray(pedidosData) ? pedidosData : []);
      if (showMessage) {
        setStatusMessage({ type: "success", text: "Datos actualizados." });
      }
    } catch (error) {
      setStatusMessage({ type: "error", text: error.message });
    } finally {
      setLoading(false);
    }
  }

  async function createBusinessUser(event) {
    event.preventDefault();
    setLoading(true);
    try {
      const created = await request(`${USUARIO_API}/api/v1/usuarios`, {
        method: "POST",
        body: JSON.stringify(businessUser)
      });
      setUsuarios((current) => [created, ...current]);
      setPedidoForm((current) => ({ ...current, usuarioId: created.id }));
      setStatusMessage({ type: "success", text: `Usuario de negocio creado con ID ${created.id}.` });
    } catch (error) {
      setStatusMessage({ type: "error", text: error.message });
    } finally {
      setLoading(false);
    }
  }

  async function createPedido(event) {
    event.preventDefault();
    setLoading(true);
    try {
      const payload = {
        ...pedidoForm,
        usuarioId: Number(pedidoForm.usuarioId),
        cantidad: Number(pedidoForm.cantidad),
        precioUnitario: Number(pedidoForm.precioUnitario)
      };

      const created = await request(`${PEDIDO_API}/api/v1/pedidos`, {
        method: "POST",
        headers: authHeaders(),
        body: JSON.stringify(payload)
      });
      setPedidos((current) => [created, ...current]);
      setStatusMessage({ type: "success", text: `Pedido creado con ID ${created.id}.` });
    } catch (error) {
      setStatusMessage({ type: "error", text: error.message });
    } finally {
      setLoading(false);
    }
  }

  async function loadPedidoDetails(id) {
    setLoading(true);
    try {
      const data = await request(`${PEDIDO_API}/api/v1/pedidos/${id}/detalles`, {
        headers: authHeaders()
      });
      setSelectedPedido(data);
    } catch (error) {
      setStatusMessage({ type: "error", text: error.message });
    } finally {
      setLoading(false);
    }
  }

  async function changePedidoState(id, action) {
    setLoading(true);
    try {
      const updated = await request(`${PEDIDO_API}/api/v1/pedidos/${id}/${action}`, {
        method: "PUT",
        headers: authHeaders()
      });
      setPedidos((current) => current.map((pedido) => (pedido.id === id ? updated : pedido)));
      setStatusMessage({ type: "success", text: `Pedido ${id} actualizado.` });
    } catch (error) {
      setStatusMessage({ type: "error", text: error.message });
    } finally {
      setLoading(false);
    }
  }

  async function testWithoutToken() {
    setLoading(true);
    try {
      const response = await fetch(`${PEDIDO_API}/api/v1/pedidos`);
      if (response.status === 401) {
        setStatusMessage({ type: "success", text: "Pedido Service rechazo la consulta sin token con 401." });
      } else {
        setStatusMessage({ type: "error", text: `Se esperaba 401 y llego ${response.status}.` });
      }
    } catch (error) {
      setStatusMessage({ type: "error", text: error.message });
    } finally {
      setLoading(false);
    }
  }

  return (
    <main className="app-shell">
      <aside className="side-panel">
        <div className="brand-block">
          <ShieldCheck aria-hidden="true" />
          <div>
            <h1>Microservicios Console</h1>
            <p>Auth, usuarios y pedidos en un solo flujo.</p>
          </div>
        </div>

        <form className="tool-panel" onSubmit={login}>
          <div className="panel-heading">
            <LogIn aria-hidden="true" />
            <h2>Autenticacion</h2>
          </div>
          <Field
            label="Username"
            value={authForm.username}
            onChange={(value) => setAuthForm({ ...authForm, username: value })}
          />
          <Field
            label="Password"
            type="password"
            value={authForm.password}
            onChange={(value) => setAuthForm({ ...authForm, password: value })}
          />
          <div className="button-row">
            <button type="button" className="secondary-button" onClick={createAuthUser} disabled={loading}>
              <UserPlus size={18} aria-hidden="true" />
              Crear usuario
            </button>
            <button type="submit" className="primary-button" disabled={loading}>
              <LogIn size={18} aria-hidden="true" />
              Login
            </button>
          </div>
          {authMessage && <Notice type={authMessage.type} text={authMessage.text} />}
        </form>

        <section className="session-panel">
          <div>
            <span className={`session-dot ${isAuthenticated ? "active" : ""}`} />
            {isAuthenticated ? `Sesion: ${authUser}` : "Sesion no iniciada"}
          </div>
          <code>{tokenPreview}</code>
          <button type="button" className="ghost-button" onClick={logout} disabled={!isAuthenticated}>
            <LogOut size={18} aria-hidden="true" />
            Logout
          </button>
        </section>
      </aside>

      <section className="workspace">
        <header className="topbar">
          <div>
            <p className="eyebrow">Sprint auth token</p>
            <h2>Operacion protegida por JWT</h2>
          </div>
          <div className="topbar-actions">
            <button type="button" className="icon-button" title="Probar rechazo sin token" onClick={testWithoutToken}>
              <AlertCircle size={20} aria-hidden="true" />
            </button>
            <button type="button" className="icon-button" title="Actualizar datos" onClick={() => refreshData()} disabled={!isAuthenticated}>
              <RefreshCw size={20} aria-hidden="true" />
            </button>
          </div>
        </header>

        {statusMessage && <Notice type={statusMessage.type} text={statusMessage.text} />}

        <div className="metrics-row">
          <Metric label="Usuarios" value={usuarios.length} tone="green" />
          <Metric label="Pedidos" value={pedidos.length} tone="blue" />
          <Metric label="Token" value={isAuthenticated ? "Activo" : "Faltante"} tone={isAuthenticated ? "green" : "red"} />
        </div>

        <div className="content-grid">
          <form className="tool-panel" onSubmit={createBusinessUser}>
            <div className="panel-heading">
              <Users aria-hidden="true" />
              <h3>Usuario de negocio</h3>
            </div>
            <div className="two-column">
              <Field label="Email" value={businessUser.email} onChange={(value) => setBusinessUser({ ...businessUser, email: value })} />
              <Field label="Telefono" value={businessUser.telefono} onChange={(value) => setBusinessUser({ ...businessUser, telefono: value })} />
              <Field label="Nombre" value={businessUser.nombre} onChange={(value) => setBusinessUser({ ...businessUser, nombre: value })} />
              <Field label="Apellido" value={businessUser.apellido} onChange={(value) => setBusinessUser({ ...businessUser, apellido: value })} />
              <Field label="Ciudad" value={businessUser.ciudad} onChange={(value) => setBusinessUser({ ...businessUser, ciudad: value })} />
              <Field label="Pais" value={businessUser.pais} onChange={(value) => setBusinessUser({ ...businessUser, pais: value })} />
            </div>
            <Field label="Direccion" value={businessUser.direccion} onChange={(value) => setBusinessUser({ ...businessUser, direccion: value })} />
            <button type="submit" className="primary-button" disabled={loading}>
              <UserPlus size={18} aria-hidden="true" />
              Crear usuario
            </button>
          </form>

          <form className="tool-panel" onSubmit={createPedido}>
            <div className="panel-heading">
              <PackagePlus aria-hidden="true" />
              <h3>Pedido protegido</h3>
            </div>
            <div className="two-column">
              <Field label="Usuario ID" type="number" value={pedidoForm.usuarioId} onChange={(value) => setPedidoForm({ ...pedidoForm, usuarioId: value })} />
              <Field label="Producto ID" value={pedidoForm.numeroProducto} onChange={(value) => setPedidoForm({ ...pedidoForm, numeroProducto: value })} />
              <Field label="Producto" value={pedidoForm.nombreProducto} onChange={(value) => setPedidoForm({ ...pedidoForm, nombreProducto: value })} />
              <Field label="Cantidad" type="number" value={pedidoForm.cantidad} onChange={(value) => setPedidoForm({ ...pedidoForm, cantidad: value })} />
              <Field label="Precio" type="number" value={pedidoForm.precioUnitario} onChange={(value) => setPedidoForm({ ...pedidoForm, precioUnitario: value })} />
              <Field label="Envio" value={pedidoForm.direccionEnvio} onChange={(value) => setPedidoForm({ ...pedidoForm, direccionEnvio: value })} />
            </div>
            <Field label="Descripcion" value={pedidoForm.descripcion} onChange={(value) => setPedidoForm({ ...pedidoForm, descripcion: value })} />
            <button type="submit" className="primary-button" disabled={loading || !isAuthenticated}>
              <PackagePlus size={18} aria-hidden="true" />
              Crear pedido
            </button>
          </form>
        </div>

        <section className="data-band">
          <div className="list-panel">
            <div className="panel-heading">
              <Users aria-hidden="true" />
              <h3>Usuarios</h3>
            </div>
            <div className="table-wrap">
              <table>
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>Nombre</th>
                    <th>Email</th>
                    <th>Activo</th>
                  </tr>
                </thead>
                <tbody>
                  {usuarios.map((usuario) => (
                    <tr key={usuario.id}>
                      <td>{usuario.id}</td>
                      <td>{usuario.nombre} {usuario.apellido}</td>
                      <td>{usuario.email}</td>
                      <td>{usuario.activo ? "Si" : "No"}</td>
                    </tr>
                  ))}
                  {!usuarios.length && <EmptyRow columns={4} text="Sin usuarios cargados" />}
                </tbody>
              </table>
            </div>
          </div>

          <div className="list-panel">
            <div className="panel-heading">
              <ClipboardList aria-hidden="true" />
              <h3>Pedidos</h3>
            </div>
            <div className="table-wrap">
              <table>
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>Producto</th>
                    <th>Estado</th>
                    <th>Total</th>
                    <th>Acciones</th>
                  </tr>
                </thead>
                <tbody>
                  {pedidos.map((pedido) => (
                    <tr key={pedido.id}>
                      <td>{pedido.id}</td>
                      <td>{pedido.nombreProducto}</td>
                      <td><span className="state-pill">{pedido.estado}</span></td>
                      <td>${Number(pedido.precioTotal || 0).toFixed(2)}</td>
                      <td>
                        <div className="row-actions">
                          <button type="button" onClick={() => loadPedidoDetails(pedido.id)}>Detalle</button>
                          <button type="button" onClick={() => changePedidoState(pedido.id, "confirmar")}>Confirmar</button>
                          <button type="button" onClick={() => changePedidoState(pedido.id, "cancelar")}>Cancelar</button>
                        </div>
                      </td>
                    </tr>
                  ))}
                  {!pedidos.length && <EmptyRow columns={5} text="Sin pedidos disponibles" />}
                </tbody>
              </table>
            </div>
          </div>
        </section>

        {selectedPedido && (
          <section className="detail-band">
            <div className="panel-heading">
              <CheckCircle2 aria-hidden="true" />
              <h3>Detalle del pedido #{selectedPedido.pedido?.id}</h3>
            </div>
            <pre>{JSON.stringify(selectedPedido, null, 2)}</pre>
          </section>
        )}
      </section>
    </main>
  );
}

function Field({ label, value, onChange, type = "text" }) {
  return (
    <label className="field">
      <span>{label}</span>
      <input type={type} value={value} onChange={(event) => onChange(event.target.value)} />
    </label>
  );
}

function Notice({ type, text }) {
  return (
    <div className={`notice ${type}`}>
      {type === "success" ? <CheckCircle2 size={18} aria-hidden="true" /> : <AlertCircle size={18} aria-hidden="true" />}
      <span>{text}</span>
    </div>
  );
}

function Metric({ label, value, tone }) {
  return (
    <div className={`metric ${tone}`}>
      <span>{label}</span>
      <strong>{value}</strong>
    </div>
  );
}

function EmptyRow({ columns, text }) {
  return (
    <tr>
      <td colSpan={columns} className="empty-cell">{text}</td>
    </tr>
  );
}

function parseJson(text) {
  try {
    return JSON.parse(text);
  } catch {
    return text;
  }
}

export default App;
