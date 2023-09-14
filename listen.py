import socket
import threading
import mysql.connector

def udp_server(port):
    s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    s.bind(('0.0.0.0', port))
    print(f"UDP server listening on port {port}")

    while True:
        data, addr = s.recvfrom(65535)  # Tamaño máximo UDP
        print(f"Received UDP data from {addr}: {data.decode('utf-8')}")

        try:
            connection = mysql.connector.connect(
                host="database-1.cvdwcehcq5em.us-east-2.rds.amazonaws.com",
                user="Buabs",
                password="buabs123",
                database="BuabsBD"
            )

            cursor = connection.cursor()

            data_to_insert = data.decode("utf-8")
            latitud, longitud, fecha, hora = data_to_insert.split(',') # Asumiendo que los valores están separados por comas

            insert_query = "INSERT INTO tabla (LATITUD, LONGITUD, FECHA, HORA) VALUES (%s, %s, %s, %s)"
            cursor.execute(insert_query, (latitud, longitud, fecha, hora))

            connection.commit()
            print("Datos insertados correctamente en la base de datos.")

        except mysql.connector.Error as err:
            print(f"Error during database operation: {err}")
            if 'connection' in locals() or 'connection' in globals():
                connection.rollback()  # Rollback changes if an error occurs

        finally:
            if 'cursor' in locals() or 'cursor' in globals():
                cursor.close()
            if 'connection' in locals() or 'connection' in globals():
                connection.close()

if __name__ == "__main__":
    port = 25000
    udp_thread = threading.Thread(target=udp_server, args=(port,))
    udp_thread.start()
    udp_thread.join()
