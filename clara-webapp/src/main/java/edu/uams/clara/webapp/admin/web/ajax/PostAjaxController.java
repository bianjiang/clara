package edu.uams.clara.webapp.admin.web.ajax;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.collect.Lists;

import edu.uams.clara.webapp.common.dao.post.PostDao;
import edu.uams.clara.webapp.common.domain.post.Post;
import edu.uams.clara.webapp.common.util.response.JsonResponse;

@Controller
public class PostAjaxController {

	private final static Logger logger = LoggerFactory
			.getLogger(PostAjaxController.class);

	private PostDao postDao;

	@RequestMapping(value = "/ajax/posts/list-current", method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody
	JsonResponse getPosts() {
		Boolean includeExpired = false;
		List<Post> posts = postDao.listAllOrderByDate(includeExpired);
		logger.debug(posts.get(0).getCreated()+"");
		return new JsonResponse(false, posts);
	}
	
	@RequestMapping(value = "/ajax/admin/super/posts/list", method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody
	JsonResponse getPosts(
			@RequestParam(value = "includeExpiredPosts", required = false) Boolean includeExpiredPosts) {

		Boolean includeExpired = false;
		try {
			if(includeExpiredPosts!=null){
			includeExpired = includeExpiredPosts;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		List<Post> posts = postDao.listAllOrderByDate(includeExpired);
		logger.debug(posts.get(0).getCreated()+"");
		return new JsonResponse(false, posts);
	}
	
	@RequestMapping(value = "/ajax/admin/super/posts/{id}/remove", method = RequestMethod.GET, produces = "application/json")
	public 
	@ResponseBody JsonResponse deleteNews(@PathVariable("id") int id,@RequestParam(value = "includeExpiredPosts", required = false) Boolean includeExpiredPosts){
		List<Post> posts = Lists.newArrayList();
		Boolean includeExpired = false;
		try {
			if(includeExpiredPosts!=null){
			includeExpired = includeExpiredPosts;
			}
		
		postDao.detelePost(id);
		posts = postDao.listAllOrderByDate(includeExpired);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new JsonResponse(false, posts);
	}

	@RequestMapping(value = "/ajax/admin/super/posts/create", method = RequestMethod.POST)
	public @ResponseBody
	Post createPost(@RequestBody Post post) {
		Post newPost = new Post();
		try {
			logger.debug("new post.. " + post.getTitle());

			newPost.setTitle(post.getTitle());
			newPost.setMessage(post.getMessage());
			newPost.setMessageLevel(post.getMessageLevel());
			newPost.setExpireDate(post.getExpireDate());
			newPost.setCreated(new Date());
			newPost.setRetired(Boolean.FALSE);
			postDao.findAll();
			newPost = postDao.saveOrUpdate(newPost);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return newPost;
	}

	public PostDao getPostDao() {
		return postDao;
	}

	@Autowired(required = true)
	public void setPostDao(PostDao postDao) {
		this.postDao = postDao;
	}

}
