package dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import model.Card;
import model.Player;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class DaoImpl implements Dao{
	
	private Connection connection;

    private static final String URL = "jdbc:mysql://localhost:3306/jdbc_ariannaleccese_uno";
    private static final String USUARIO = "root";
    private static final String CONTRASEÑA = "";
    
    /*
     * 
     * Este código establece una conexión a una base de datos MySQL utilizando JDBC. 
     * La URL de la base de datos, el nombre de usuario y la contraseña se definen como constantes.
     *  Luego, en el método connect(), se crea una conexión utilizando la clase DriverManager, y 
     *  se asigna a la variable connection. Si hay algún error durante la conexión, se captura la excepción 
     *  SQLException y se imprime un mensaje de error. Finalmente, se imprime un mensaje de éxito 
     *  si la conexión se establece correctamente.
     */
   
	@Override
	public void connect() throws SQLException {
		// TODO Auto-generated method stub
		
		try {
			
			String url = URL;
			String usuario = USUARIO;
			String contraseña = CONTRASEÑA;
			connection = DriverManager.getConnection(url, usuario, contraseña);
			System.out.println("Conexion establecida");
			
		}catch(SQLException ex) {
			System.out.println(ex.getMessage() + " Error al establecer conexion");
		}
	}

	@Override
	public void disconnect() throws SQLException {
		// TODO Auto-generated method stub
		
	}
	
	/*
		Este método devuelve el último ID de carta asignado 
		a un jugador específico en la tabla de la base de datos. 
		Utiliza una consulta SQL para seleccionar el máximo ID de carta asociado con el ID 
		del jugador proporcionado. Si no hay ningún ID de carta asociado con el jugador, devuelve 0. 
		Luego, incrementa este último ID obtenido en 1 para obtener el próximo ID disponible y lo devuelve. 
		Si ocurre algún error durante la ejecución de la consulta SQL, se captura la excepción SQLException 
		y se imprime un mensaje de error.
	 */
	@Override
	public int getLastIdCard(int playerId) throws SQLException {
	    int lastId = 0;
	    
	    String query = "SELECT IFNULL(MAX(id), 0) AS last_id FROM card WHERE id_player = ?";
	    try (PreparedStatement ps = connection.prepareStatement(query)) {
	        ps.setInt(1, playerId);
	        ResultSet rs = ps.executeQuery();
	        if (rs.next()) {
	            lastId = rs.getInt("last_id");
	        }
	    } catch (SQLException ex) {
	        System.out.println("Error al obtener el último ID de carta: " + ex.getMessage());
	        throw ex;
	    }
	    // Incrementar el último ID obtenido en 1 para obtener el próximo ID disponible
	    return lastId + 1;
	}
	/*
	 * Este método busca la última carta jugada en la tabla game de la base de datos. 
	 * Utiliza una consulta SQL para seleccionar el id_card asociado al último registro (MAX(id)) 
	 * en la tabla game. Luego, utiliza este id_card para recuperar la carta correspondiente mediante 
	 * el método getCard(). Si ocurre algún error durante la ejecución de la consulta SQL, se captura la excepción
	 *  SQLException y se imprime un mensaje de error. Finalmente, devuelve la última carta jugada.
	 */

	@Override
	public Card getLastCard() throws SQLException {
	    Card lastCard = null;
	    
	    // Consulta SQL para obtener la última carta jugada
	    String selectQuery = "SELECT id_card FROM game WHERE id = (SELECT MAX(id) FROM game)";
	    
	    try (PreparedStatement ps = connection.prepareStatement(selectQuery);
	         ResultSet rs = ps.executeQuery()) {
	        if (rs.next()) {
	            int cardId = rs.getInt("id_card");
	            // Utilizar el ID de la última carta para recuperar la carta correspondiente
	            lastCard = getCard(cardId);
	        }
	    } catch (SQLException ex) {
	        System.out.println("Error al obtener la última carta jugada: " + ex.getMessage());
	        throw ex;
	    }
	    
	    return lastCard;
	}

	/*
	 * Este método busca un jugador en la tabla player usando el nombre de usuario y la contraseña proporcionados. 
	 * Realiza una consulta SQL para seleccionar los campos de la tabla donde el nombre de usuario y la contraseña
	 *  coincidan con los parámetros. Luego, ejecuta la consulta y si encuentra un resultado, crea un objeto Player 
	 *  con los datos obtenidos y lo devuelve; de lo contrario, imprime un mensaje de datos incorrectos. Si hay un error,
	 *   se imprime un mensaje de error con la excepción SQLException.
	 */
	
	@Override
	public Player getPlayer(String user, String pass) throws SQLException {
	    String select = "SELECT * FROM player WHERE user = ? AND password = ?";
	    Player player = null;

	    try (PreparedStatement ps = connection.prepareStatement(select)) {
	        ps.setString(1, user);
	        ps.setString(2, pass);
	        ResultSet rs = ps.executeQuery();

	        if (rs.next()) {
	            int playerId = rs.getInt("id");
	            String playerName = rs.getString("user");
	            // Aquí se asigna el id del jugador al objeto Player
	            player = new Player(playerId, playerName, playerId, playerId);
	            System.out.println("Has entrado al juego");
	        } else {
	            System.out.println("Datos incorrectos");
	        }
	    } catch (SQLException ex) {
	        System.out.println("Error al obtener el jugador: " + ex.getMessage());
	        throw ex;
	    }
	    return player;
	}
	
	/*
	 * Este método recupera las cartas asociadas a un jugador específico desde la base de datos. 
	 * Realiza una consulta SQL que selecciona todas las cartas de la tabla card que pertenecen al 
	 * jugador con el ID proporcionado y que no están asociadas a ningún juego en la tabla game. Luego,
	 *  crea objetos Card con los datos obtenidos de cada fila del resultado de la consulta y los agrega a una lista.
	 *   Finalmente, devuelve la lista de cartas recuperadas.
	 */
	@Override
	public ArrayList<Card> getCards(int playerId) throws SQLException {
	    ArrayList<Card> cards = new ArrayList<>();
	    String select = "SELECT * FROM card LEFT JOIN game ON card.id = game.id_card WHERE card.id_player = ? AND game.id IS NULL";
	    try (PreparedStatement ps = connection.prepareStatement(select)) {
	        ps.setInt(1, playerId);
	        
	        try (ResultSet rs = ps.executeQuery()) {
	            while (rs.next()) {
	                Card card = new Card(
	                    rs.getInt("id"),
	                    rs.getString("card_number"),
	                    rs.getString("color"),
	                    rs.getInt("id_player")
	                );
	                cards.add(card);
	            }
	        }
	    }
	    return cards;
	}
	/*
	 * 
		Este método obtiene una carta específica de la base de datos mediante su ID. 
		Realiza una consulta SQL que selecciona una carta de la tabla card donde el ID coincida con el proporcionado. 
		Luego, crea un objeto Card con los datos obtenidos de la fila del resultado de la consulta y lo devuelve. 
		Si ocurre algún error durante el proceso, imprime un mensaje de error y lanza una excepción SQLException.
	 */

	@Override
	public Card getCard(int cardId) throws SQLException {
	    Card card = null;
	    
	    String selectQuery = "SELECT * FROM card WHERE id = ?";
	    
	    try (PreparedStatement ps = connection.prepareStatement(selectQuery)) {
	        ps.setInt(1, cardId);
	        
	        try (ResultSet rs = ps.executeQuery()) {
	            if (rs.next()) {
	                card = new Card(
	                    rs.getInt("id"),
	                    rs.getString("card_number"),
	                    rs.getString("color"),
	                    rs.getInt("id_player")
	                );
	            }
	        }
	    } catch (SQLException ex) {
	        System.out.println("Error al obtener la carta: " + ex.getMessage());
	        throw ex;
	    }
	    
	    return card;
	}

	/*
	 * Este método guarda un nuevo juego en la base de datos, asociado a una carta específica. 
	 * Primero desactiva el modo de autocommit de la conexión para controlar la transacción manualmente.
	 *  Luego, ejecuta una consulta SQL para insertar un nuevo juego en la tabla game, con el ID de la carta proporcionada. 
	 *  Si la inserción es exitosa, confirma la transacción manualmente. En caso de error, realiza un rollback para revertir 
	 *  los cambios y lanza una excepción SQLException. Finalmente, restaura el modo de autocommit a su valor original.
	 */
	@Override
	public void saveGame(Card card) throws SQLException {
	    // Desactivar autocommit
	    connection.setAutoCommit(false);

	    // Consulta SQL para insertar un nuevo juego con el ID de la carta
	    String insertQuery = "INSERT INTO game (id_card) VALUES (?)";
	    
	    try (PreparedStatement ps = connection.prepareStatement(insertQuery)) {
	        ps.setInt(1, card.getId()); // Establecer el ID de la carta como el valor del primer parámetro
	        
	        int rowsAffected = ps.executeUpdate();
	        if (rowsAffected > 0) {
	            System.out.println("Nuevo juego guardado correctamente con el ID de la carta: " + card.getId());
	            connection.commit(); // Confirmar la transacción manualmente
	        } else {
	            System.out.println("No se guardó ningún juego. Verifica la consulta de inserción.");
	        }
	    } catch (SQLException ex) {
	        System.out.println("Error al guardar el nuevo juego: " + ex.getMessage());
	        // En caso de error, realizar un rollback para revertir los cambios
	        connection.rollback();
	        throw ex;
	    } finally {
	        // Restaurar autocommit a su valor original
	        connection.setAutoCommit(true);
	    }
	}
	/*
	 * 
		Este método guarda una carta en la base de datos. 
		Ejecuta una consulta SQL para insertar los valores del jugador, 
		el número de carta y el color en la tabla CARD. Si ocurre algún error durante la inserción,
		 se imprime un mensaje de error y se lanza una excepción SQLException.
	 */

	@Override
	public void saveCard(Card card) throws SQLException {
	    String insertQuery = "INSERT INTO CARD (id_player, card_number, color) VALUES (?, ?, ?)";
	    
	    try (PreparedStatement ps = connection.prepareStatement(insertQuery)) {
	        // Establecer los valores de los parámetros de la declaración preparada
	        ps.setInt(1, card.getPlayerId()); // Establece el id del jugador en el primer parámetro
	        ps.setString(2, card.getNumber()); // Establece el número de carta como una cadena en el segundo parámetro
	        ps.setString(3, card.getColor());  // Establece el color de la carta como una cadena en el tercer parámetro

	        ps.executeUpdate();
	    } catch (SQLException ex) {
	        System.out.println("Error al guardar la carta: " + ex.getMessage());
	        throw ex;
	    }
	}

	/*
	 * Este método elimina una carta de la base de datos. 
	 * Primero verifica si la carta es una carta de finalización de juego (cambio de lado o salto).
	 *  Si lo es, elimina la carta de la tabla game. Si la carta no es una carta de finalización de juego,
	 *   se imprime un mensaje indicando que la carta no es una carta de finalización de juego. Si ocurre algún 
	 *   error durante la eliminación, se imprime un mensaje de error y se lanza una excepción SQLException.

	 */
	@Override
	public void deleteCard(Card card) throws SQLException {
	    // Verificar si la carta es una carta de finalización de juego (cambio de lado o salto)
	    String number = card.getNumber();
	    if ("CHANGESIDE".equalsIgnoreCase(number) || "SKIP".equalsIgnoreCase(number)) {
	        // Si es una carta de finalización de juego, eliminarla de la tabla game
	        String deleteQuery = "DELETE FROM game WHERE id_card = ?";
	        
	        try (PreparedStatement ps = connection.prepareStatement(deleteQuery)) {
	            ps.setInt(1, card.getId());
	            ps.executeUpdate();
	            System.out.println("La última carta de finalización de juego se ha eliminado correctamente.");
	        } catch (SQLException ex) {
	            System.out.println("Error al eliminar la última carta de finalización de juego: " + ex.getMessage());
	            throw ex;
	        }
	    } else {
	        System.out.println("La carta proporcionada no es una carta de finalización de juego.");
	    }
	}


	/*
	 * Este método elimina todas las cartas asociadas a un jugador de las tablas game y card. 
	 * Primero, elimina todos los registros de la tabla game asociados al jugador específico. 
	 * Luego, elimina todos los registros de la tabla card asociados al mismo jugador. 
	 * Si ocurre algún error durante la eliminación, se imprime un mensaje de error y se lanza una excepción SQLException.
	 */
	@Override
	public void clearDeck(int playerId) throws SQLException {
	    // Eliminar todos los registros de la tabla game asociados al jugador específico
	    String deleteGameQuery = "DELETE FROM game WHERE id_card IN (SELECT id FROM card WHERE id_player = ?)";

	    try (PreparedStatement ps = connection.prepareStatement(deleteGameQuery)) {
	        ps.setInt(1, playerId);
	        ps.executeUpdate();
	        System.out.println("Todos los registros de la tabla game asociados al jugador " + playerId + " han sido eliminados.");
	    } catch (SQLException ex) {
	        System.out.println("Error al limpiar la tabla game: " + ex.getMessage());
	        throw ex;
	    }
	    
	    // Eliminar todos los registros de la tabla card asociados al jugador específico
	    String deleteCardQuery = "DELETE FROM card WHERE id_player = ?";
	    
	    try (PreparedStatement ps = connection.prepareStatement(deleteCardQuery)) {
	        ps.setInt(1, playerId);
	        int rowsDeleted = ps.executeUpdate();
	        System.out.println("Número de filas eliminadas de la tabla card: " + rowsDeleted);
	    } catch (SQLException ex) {
	        System.out.println("Error al limpiar la tabla card: " + ex.getMessage());
	        throw ex;
	    }
	}

	/*
	 * Este método actualiza el número de victorias de un jugador en la tabla player sumando 1 a la cantidad existente.
	 */
	@Override
	public void addVictories(int playerId) throws SQLException {
	    String updateQuery = "UPDATE player SET victories = victories + 1 WHERE id = ?";
	    
	    try (PreparedStatement ps = connection.prepareStatement(updateQuery)) {
	        ps.setInt(1, playerId);
	        int rowsAffected = ps.executeUpdate();
	        
	        if (rowsAffected > 0) {
	            System.out.println("Se ha actualizado el número de victorias del jugador con ID " + playerId);
	        } else {
	            System.out.println("No se pudo actualizar el número de victorias del jugador con ID " + playerId);
	        }
	    } catch (SQLException ex) {
	        System.out.println("Error al actualizar las victorias del jugador: " + ex.getMessage());
	        throw ex;
	    }
	}
	/*
	 * Este método actualiza el número de juegos de un jugador en la tabla player sumando 1 a la cantidad existente.
	 */

	@Override
	public void addGames(int playerId) throws SQLException {
	    String updateQuery = "UPDATE player SET games = games + 1 WHERE id = ?";
	    
	    try (PreparedStatement ps = connection.prepareStatement(updateQuery)) {
	        ps.setInt(1, playerId);
	        int rowsAffected = ps.executeUpdate();
	        
	        if (rowsAffected > 0) {
	            System.out.println("Se ha actualizado el número de juegos del jugador con ID " + playerId);
	        } else {
	            System.out.println("No se pudo actualizar el número de juegos del jugador con ID " + playerId);
	        }
	    } catch (SQLException ex) {
	        System.out.println("Error al actualizar los juegos del jugador: " + ex.getMessage());
	        throw ex;
	    }
	}

}
