out_file = "/Users/jean/Dropbox/1_CURRENT/INF555/INF555_Project/src/OFF/plane.off"

x = 20 #number of vertices on on edge
y = 20 #number of vertices on the other edge

center = True

x_step = 0.5
y_step = 0.5

opening_string = "OFF \n"
vertex_string = ""
graph_string = "\n"
number_of_vertices = 0
number_of_faces = 0

for j in range(0,y) :
	for i in range(0,x) :
		if center :
			vertex_string += str(x_step * (i - x/2)) + " " + str(y_step * (j-y/2)) + " 0.0\n"
		else :
			vertex_string += str(x_step * i) + " " + str(y_step * j) + " 0.0\n"
		number_of_vertices += 1 
		if i < x-1  and j > 0:
			number_of_faces += 2 
			graph_string += "3 " + str( i + j*x ) + " " + str(i+ (j-1)*x) + " " + str( i+1 + (j-1)*x) + "\n"
			graph_string += "3 " + str( i + j*x ) + " " + str(i + 1 + j*x) + " " + str( i+1 + (j-1) * x) + "\n"

opening_string += str(number_of_vertices) + " " + str(number_of_faces) + " 0\n"
#Fill the strings up

planeOff = open(out_file,'w');
planeOff.write(opening_string+vertex_string+graph_string)
planeOff.close()