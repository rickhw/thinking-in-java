# Define the route
@app.route('/download')
def download_file():
    # This is the file we want to download whenever a client requests
    file_dir = 'python/public/stream/data/'
    file_name = "heuristics.bin"
    file_path = file_dir + file_name
    chunk_size = 1024
    
    # Create chunks and yield them whenever required
    def generate():
        # Chunks size of each chink of data     
        with open(file_path, 'rb') as file:
            while True:
                chunk = file.read(chunk_size)
                if not chunk:
                    break
                yield chunk

    response = Response(

        # stream_with_context is a Flask functionality to send files in chunks
        # You can read more about it here https://flask.palletsprojects.com/en/1.0.x/patterns/streaming/
        stream_with_context(generate()),
        mimetype='application/octet-stream'
    )
    response.headers.set('Content-Disposition', 'attachment', filename=file_name)
    return response